package com.example.noteapp3

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.Post
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.StreamUrl
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale



class AddPostActivity : AppCompatActivity() {

    //private var posts: MutableList<Post> = mutableListOf()
    private lateinit var db: AppDatabase
    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var streamUrl: EditText
    private lateinit var acceptBtn: ImageButton
    private lateinit var cancelBtn: ImageButton
    private lateinit var camera: ImageButton
    private lateinit var accessFilesBtn: ImageButton
    private var mediaItems: MutableList<ViewPagerItem> = mutableListOf()
    private var currentPhotoPath: String? = null

    private lateinit var locationSwitch: androidx.appcompat.widget.SwitchCompat

    var x = 0.0
    var y = 0.0

    private lateinit var viewPager: ViewPager
    private lateinit var circleIndicator: CircleIndicator
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private val REQUEST_IMAGE_CAPTURE = 1
    private val FILE_SELECT_CODE = 2
    private val REQUEST_CAMERA_PERMISSION = 3
    private val REQUEST_LOCATION_PERMISSION = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        title = findViewById(R.id.title)
        desc = findViewById(R.id.description)
        streamUrl = findViewById(R.id.live_url)
        acceptBtn = findViewById(R.id.accept_and_post)
        cancelBtn = findViewById(R.id.cancel_button)
        camera = findViewById(R.id.button_take_photo)
        accessFilesBtn = findViewById(R.id.button_files)

        locationSwitch = findViewById(R.id.enable_location)
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLocation(this)
            } else {
                x = 0.0
                y = 0.0
            }
        }

        val profileId = intent.getStringExtra("profile_id") ?: "-1"

        lifecycleScope.launch {
            db = AppDatabase.getDatabase(this@AddPostActivity)
            //posts = getPostsFromDatabase().toMutableList()
        }

        viewPager = findViewById(R.id.viewPager)
        viewPagerAdapter = ViewPagerAdapter(mediaItems, this)
        viewPager.adapter = viewPagerAdapter
        circleIndicator = findViewById(R.id.indicator)
        circleIndicator.setViewPager(viewPager)

        if (mediaItems.isEmpty()) {
            viewPager.visibility = View.GONE
        }

        camera.setOnClickListener {
            dispatchTakePictureIntent()
        }
        accessFilesBtn.setOnClickListener {
            selectFile()
        }

        cancelBtn.setOnClickListener {
            finish()
            Toast.makeText(this, "Post canceled", Toast.LENGTH_SHORT).show()
        }

        acceptBtn.setOnClickListener {
            val post = Post()
            val titleStr = title.text.toString().trim()
            if (titleStr.isEmpty()) {
                title.error = "Title can't be empty"
            } else if (desc.text.toString().trim().isEmpty()){
                desc.error = "Description can't be empty"
            }

            else{
                post.authorId = profileId
                post.title = titleStr
                post.desc = desc.text.toString().trim()
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1 // Month starts from 0
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val formattedDate = "$year-$month-$day"
                post.date = formattedDate

                val currentTime = Date()
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                post.time = formatter.format(currentTime)
                post.imgBase64 = encodeMediaItems(mediaItems)
                post.x = x
                post.y = y
                if (streamUrl.text.isBlank()) {
                    addPost(post)
                } else {
                    val url = streamUrl.text.toString()
                    if (isLiveStreamUrl(url)) {
                        addStream(url, post)
                    } else {
                        // Handle non-live stream URL if necessary, or decide to add the post directly
                        addPost(post)
                    }
                }
            }
        }
    }

    fun isLiveStreamUrl(url: String): Boolean {
        // You can adjust this to match your live stream URL patterns
        return url.contains("youtube.com/live") || url.contains("youtu.be/live")
    }


    private fun addStream(url: String, post: Post) {

        val apiService = RetroFitClient.apiService

        val link = StreamUrl()
        link.url = url
        val call: Call<Void> = apiService.addLiveUrl(link)
        call.enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    lifecycleScope.launch {
                        addLiveUrlInApp(link)
                    }
                    addPost(post)
                }
                else{
                    Log.e("AddStreamError", "Error: ${response.code()} ${response.message()}")
                    Toast.makeText(this@AddPostActivity, "Error couldn't add stream", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddPostActivity, "Error check connection", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private suspend fun addLiveUrlInApp(link: StreamUrl) {
        withContext(Dispatchers.IO) {
            db.liveStreamUrlsDao().insert(link)
        }
    }

    private fun addPost(post: Post) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.addPost(post)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    lifecycleScope.launch {
                        addPostInApp(post)
                    }
                    val layout = layoutInflater.inflate(R.layout.success_toast, null)
                    Toast(this@AddPostActivity).apply {
                        duration = Toast.LENGTH_SHORT
                        setGravity(Gravity.BOTTOM, 0, 0)
                        view = layout
                        show()
                    }
                    finish()
                } else {
                    val layout = layoutInflater.inflate(R.layout.fail_toast, null)
                    Toast(this@AddPostActivity).apply {
                        duration = Toast.LENGTH_SHORT
                        setGravity(Gravity.BOTTOM, 0, 0)
                        view = layout
                        show()
                    }
                    finish()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                val layout = layoutInflater.inflate(R.layout.fail_toast, null)
                Toast(this@AddPostActivity).apply {
                    duration = Toast.LENGTH_SHORT
                    setGravity(Gravity.BOTTOM, 0, 0)
                    view = layout
                    show()
                }
                finish()
            }
        })
    }

    private suspend fun getPostsFromDatabase(): List<Post> {
        return withContext(Dispatchers.IO) {
            db.postDao().getAllPosts()
        }
    }

    private suspend fun addPostInApp(post: Post) {
        withContext(Dispatchers.IO) {
            db.postDao().addPost(post)
        }
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If the camera permission is not granted, request the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            // If permission is already granted, proceed with taking the picture
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        ex.printStackTrace()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "${packageName}.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        circleIndicator.setViewPager(viewPager)
                    }
                }
            }
            viewPager.visibility= View.VISIBLE
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */

        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                    val imageItem = ViewPagerItem.ImageItem(bitmap)
                    mediaItems.add(imageItem)
                    viewPagerAdapter.notifyDataSetChanged()
                    currentPhotoPath = null
                    viewPager.visibility = View.VISIBLE
                }
                FILE_SELECT_CODE -> {
                    data?.data?.also { uri ->
                        if (uri.toString().contains("image")) {
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                            val imageItem = ViewPagerItem.ImageItem(bitmap)
                            mediaItems.add(imageItem)
                        } else if (uri.toString().contains("video")) {
                            val videoItem = ViewPagerItem.VideoItem(uri)
                            Log.d("AddPostVideo",uri.toString())
                            mediaItems.add(videoItem)
                        }
                        viewPagerAdapter.notifyDataSetChanged()
                        viewPager.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    private fun encodeMediaItems(mediaItems: List<ViewPagerItem>): List<String> {
        val mediaList = mutableListOf<String>()
        for (item in mediaItems) {
            when (item) {
                is ViewPagerItem.ImageItem -> {
                    val encodedImage = encodeImage(item.bitmap)
                    mediaList.add(encodedImage)
                }
                is ViewPagerItem.VideoItem -> {
                    mediaList.add("vid:${item.uri}")
                }
            }
        }
        return mediaList
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*,video/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_SELECT_CODE)
    }

    private fun getLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    x = it.latitude
                    y = it.longitude
                }
            }
        }
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("You have denied a required permission. Please go to settings and enable it.")
            .setPositiveButton("Go to Settings") { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, proceed with taking the picture
                    dispatchTakePictureIntent()
                } else {
                    // Permission was denied, inform the user with a message or handle it appropriately
                    Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show()
                    showPermissionSettingsDialog()
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, proceed with getting location
                    getLocation(this)
                } else {
                    // Permission was denied, inform the user with a message or handle it appropriately
                    locationSwitch.isChecked = false
                    Toast.makeText(this, "GPS permission is required to get location", Toast.LENGTH_SHORT).show()
                    showPermissionSettingsDialog()
                }
            }
        }
    }



}
