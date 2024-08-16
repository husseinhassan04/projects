package com.example.noteapp3

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.noteapp3.RecyclerView.CommentsAdapter
import com.example.noteapp3.RecyclerView.ImagePagerAdapter
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.Comment
import com.example.noteapp3.models.Post
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var newComment: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var loadingIndicator : ProgressBar
    private var post = Post()

    private var profiles: Map<String, profile> = emptyMap()

    private lateinit var title: TextView
    private lateinit var desc: TextView
    private lateinit var views: TextView
    private lateinit var date: TextView
    private lateinit var time: TextView
    private lateinit var imagesViewPager: ViewPager
    private lateinit var indicator: CircleIndicator

    private var posts: List<Post> = emptyList()
    private lateinit var comments: MutableList<Comment>
    private lateinit var postId: String
    private lateinit var loggedUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = AppDatabase.getDatabase(this)


        loadingIndicator = findViewById(R.id.loadingIndicator)

        // Start loading indicator
        loadingIndicator.visibility = View.VISIBLE
        fetchProfiles()

        //get post details and match with the views
        getPost()
        title = findViewById(R.id.title)
        desc = findViewById(R.id.description)
        views = findViewById(R.id.views)
        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        imagesViewPager = findViewById(R.id.images)
        indicator = findViewById(R.id.indicator)
        sendButton = findViewById(R.id.sendButton)
        newComment = findViewById(R.id.newComment)

        postId = intent.getStringExtra("post_id") ?: "-1"
        loggedUserId = intent.getStringExtra("profile_id") ?: "-1"







        //get list of comments ids from extra
        val commentIds = intent.getStringArrayListExtra("comment_ids") ?: emptyList<String>()
        loadComments(commentIds)

        //add a new comment
        sendButton.setOnClickListener {
            val comment = Comment()
            if (newComment.text.isEmpty()) {
                Toast.makeText(this, "Comment can't be empty", Toast.LENGTH_SHORT).show()
            } else {
                comment.text = newComment.text.toString()
                comment.profileId = loggedUserId
                comment.postId = postId

                addComment(comment)

                commentsAdapter.updateComments(comments)
                newComment.text.clear()

                //add the comment to the list of comments in the post
                post.comments.add(comment.id)
                updatePost(post)

            }
        }
    }

    private suspend fun getPostsFromDatabase(): List<Post> {
        return withContext(Dispatchers.IO) {
            db.postDao().getAllPosts()
        }
    }

    private fun getPost() {
        lifecycleScope.launch {
            posts = getPostsFromDatabase()
            val getPost =posts.find { it.id.toString() == postId }
            post = getPost ?: Post()
            if (post.title.isNotEmpty()) {
                title.text = post.title
                desc.text = post.desc
                views.text = post.views.toString()
                date.text = post.date
                time.text = post.time

                val images = post.imgBase64
                if (images.isNotEmpty()) {
                    val adapter = ImagePagerAdapter(images,this@DetailsActivity)
                    imagesViewPager.adapter = adapter
                    indicator.setViewPager(imagesViewPager)
                } else {
                    imagesViewPager.adapter = null
                    imagesViewPager.visibility = View.GONE
                }
            } else {
                Log.e("DetailsActivity", "Post not found with ID: $postId")
                Toast.makeText(this@DetailsActivity, "Post not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadComments(commentIds: List<String>) {
        lifecycleScope.launch {
            comments = getCommentsByIds(commentIds)

            commentsAdapter = CommentsAdapter(comments)
            recyclerView.adapter = commentsAdapter
            setRecyclerViewHeight()
//            commentsAdapter.updateComments(comments)
        }
    }

    private suspend fun getCommentsByIds(commentIds: List<String>): MutableList<Comment> {
        return withContext(Dispatchers.IO) {
            db.commentDao().getCommentsByIds(commentIds).toMutableList()
        }
    }

    private fun addComment(comment: Comment) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.addComment(comment)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    //Handle successful posting
                    lifecycleScope.launch {
                        addCommentInApp(comment)
                    }

                } else {
                    Toast.makeText(this@DetailsActivity, "Comment wasn't added in app", Toast.LENGTH_SHORT)
                        .show()


                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@DetailsActivity, "Comment wasn't added on server", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun addCommentInApp(comment: Comment) {
        withContext(Dispatchers.IO) {
            val commentDao = db.commentDao()

            commentDao.addComment(comment)
            withContext(Dispatchers.Main) {
                comments.add(comment)
                commentsAdapter.notifyItemInserted(comments.size - 1)
                setRecyclerViewHeight()
            }
        }
    }


    private fun updatePost(post: Post) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.updatePost(post.id,post)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {

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

    private fun fetchProfiles() {
        val apiService = RetroFitClient.apiService
        val call = apiService.getAllProfiles()

        call.enqueue(object : Callback<List<profile>> {
            override fun onResponse(call: Call<List<profile>>, response: Response<List<profile>>) {
                if (response.isSuccessful) {
                    val profilesList = response.body()
                    profilesList?.let {
                        profiles = it.associateBy { profile -> profile.id }
                        updateUIWithProfileData()
                    } ?: run {
                        Log.e(ContentValues.TAG, "Empty response body")
                    }
                } else {
                    Log.e(ContentValues.TAG, "Response not successful: ${response.code()}")
                }
                // Hide loading indicator
                loadingIndicator.visibility = View.GONE
            }

            override fun onFailure(call: Call<List<profile>>, t: Throwable) {
                Log.e(ContentValues.TAG, "Error fetching profiles", t)
                // Hide loading indicator
                loadingIndicator.visibility = View.GONE
            }
        })
    }

    private fun updateUIWithProfileData() {

        val picture: CircleImageView = findViewById(R.id.authorPicture)
        val username: TextView = findViewById(R.id.authorUsername)
        val author = profiles[post.authorId]
        username.text = author?.user ?: "Unknown"

        val pictureBase64 = author?.picture
        val bitmap = decodeBase64(pictureBase64 ?: "")
        if (bitmap != null) {
            picture.setImageBitmap(bitmap)
        } else {
            picture.setImageResource(R.drawable.default_profile_picture) // Set a default picture
        }
    }

    private fun setRecyclerViewHeight() {
        recyclerView.post {
            val itemHeight = recyclerView.getChildAt(0)?.height ?: 0
            val itemCount = recyclerView.adapter?.itemCount ?: 0
            val totalHeight = itemHeight * itemCount

            val layoutParams = recyclerView.layoutParams
            layoutParams.height = totalHeight
            recyclerView.layoutParams = layoutParams
        }
    }



}
