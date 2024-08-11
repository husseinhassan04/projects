package com.example.noteapp3

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.Post
import com.example.noteapp3.models.RetroFitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback{

    private lateinit var googleMap: GoogleMap
    private val markerMap = mutableMapOf<Marker, String>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionRequestCode = 1001
    private lateinit var progressBar: ProgressBar
    private lateinit var db: AppDatabase
    private var posts: List<Post> = emptyList()
    private lateinit var profileId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        db = AppDatabase.getDatabase(requireContext())
        DataFetcher.fetchDataAndStore(requireContext(),db) {}
        profileId = arguments?.getString("profile_id") ?: "-1"
        val app = requireActivity().application as User
        val userDetails = app.getUserDetails()
        profileId = userDetails[SessionManager.KEY_USER_ID]?:"-1"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            showCurrentLocation()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode)
        }

        // Show the progress bar
        progressBar.visibility = View.VISIBLE

        // Load markers in a background thread
        lifecycleScope.launch {
            loadMarkers()
            // Hide the progress bar once loading is done
            progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun loadMarkers() {
        posts = getPostsFromDatabase()
        Log.d("MapFragment", "Posts retrieved: ${posts.size}")

        val boundsBuilder = LatLngBounds.Builder()

        withContext(Dispatchers.Main) {
            for (post in posts) {
                Log.d("MapFragment", "Adding marker: ${post.title} at (${post.y}, ${post.x})")
                if (post.x != 0.0 && post.y != 0.0) {
                    addMarker(post.x, post.y, post.title)
                    boundsBuilder.include(LatLng(post.x, post.y))
                }
            }

            // Update camera to show all markers
            val bounds = boundsBuilder.build()
            val padding = 100 // offset from edges of the map in pixels
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

            googleMap.setOnMarkerClickListener { marker ->
                val markerPosition = marker.position
                var position: Int = -1
                var postId: String = ""
                var postAuthor: String = ""

                val post = posts.find { it.x == markerPosition.latitude && it.y == markerPosition.longitude }

                post?.let { foundPost ->
                    position = posts.indexOf(foundPost)
                    postId = foundPost.id
                    postAuthor = foundPost.authorId

                    if (postAuthor.toString() != profileId) {
                        posts[position].views++
                        updatePost(postId, position)
                    }

                    val intent = Intent(requireContext(), DetailsActivity::class.java)
                    intent.putExtra("post_id", foundPost.id)
                    intent.putExtra("profile_id", profileId)
                    startActivity(intent)
                }

                true
            }

        }
    }


    private fun showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        googleMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                Toast.makeText(requireContext(), "Unable to retrieve current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarker(long: Double, lat: Double, title: String = "Marker") {
        val position = LatLng(long, lat)
        val marker = googleMap.addMarker(MarkerOptions().position(position).title(title))
        marker?.let {
            markerMap[it] = title
        }
    }

    private suspend fun getPostsFromDatabase(): List<Post> {
        return withContext(Dispatchers.IO) {
            db.postDao().getAllPosts()
        }
    }
    private fun updatePost(postId: String,position: Int) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.updatePost(postId,posts[position])
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Handle successful update
                    println("Post updated successfully")
                } else {
                    // Handle unsuccessful response
                    println("Failed to update post: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Handle failure
                println("Error: ${t.message}")
            }
        })

    }
}
