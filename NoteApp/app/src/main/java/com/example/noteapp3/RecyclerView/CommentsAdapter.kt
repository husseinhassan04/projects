package com.example.noteapp3.RecyclerView

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp3.R
import com.example.noteapp3.models.Comment
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentsAdapter(private var comments: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private var profiles: Map<String, profile> = emptyMap()

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val authorPicture: ImageView = view.findViewById(R.id.picture)
        val authorTextView: TextView = view.findViewById(R.id.username)
        val commentTextView: TextView = view.findViewById(R.id.commentInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val profile = profiles[comment.profileId]
        val pictureBase64 = profile?.picture
        val bitmap = decodeBase64(pictureBase64?: "")
        if (bitmap != null) {
            holder.authorPicture.setImageBitmap(bitmap)
        } else {
            holder.authorPicture.setImageResource(R.drawable.default_profile_picture) // Set a default picture
        }
        holder.authorTextView.text  = profile?.user
        holder.commentTextView.text = comment.text
    }

    override fun getItemCount() = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    private fun fetchProfiles() {
        val apiService = RetroFitClient.apiService
        val call = apiService.getAllProfiles()

        call.enqueue(object : Callback<List<profile>> {
            override fun onResponse(call: Call<List<profile>>, response: Response<List<profile>>) {
                if (response.isSuccessful) {
                    val profilesList = response.body()
                    profilesList?.let {
                        profiles = it.associateBy { profile -> profile.id }
                        notifyDataSetChanged() // Notify the adapter to refresh the list
                    } ?: run {
                        Log.e(ContentValues.TAG, "Empty response body")
                    }
                } else {
                    Log.e(ContentValues.TAG, "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<profile>>, t: Throwable) {
                Log.e(ContentValues.TAG, "Error fetching profiles", t)
            }
        })
    }

    init {
        fetchProfiles() // Fetch profiles when the adapter is initialized
    }

    private fun decodeBase64(base64Str: String): Bitmap? {
        return try {
            if (base64Str.isNotEmpty()) {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            Log.e(ContentValues.TAG, "Error decoding Base64 string", e)
            null
        }
    }
}
