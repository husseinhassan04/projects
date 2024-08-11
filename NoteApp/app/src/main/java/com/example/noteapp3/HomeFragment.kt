package com.example.noteapp3

import PostsAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.Post
import com.example.noteapp3.models.RetroFitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.ResolverStyle
import org.threeten.bp.format.SignStyle
import org.threeten.bp.temporal.ChronoField

class HomeFragment : Fragment(), PostsAdapter.OnItemClickListener, PostsAdapter.OnPostItemClickListener {

    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter

    private lateinit var frame: FrameLayout
    private var posts: MutableList<Post> = mutableListOf()
    private lateinit var profileId: String
    private lateinit var userName: String
    private var nextCommentId: Int= -1


    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loading: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        profileId = arguments?.getString("profile_id") ?: "-1"
        userName = arguments?.getString("name") ?: "User"


        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        loading = view.findViewById(R.id.progressBar)

        db = AppDatabase.getDatabase(requireContext())
        fetchPosts()

        frame = view.findViewById(R.id.frame)


        recyclerView = view.findViewById(R.id.feed_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsAdapter = PostsAdapter(posts, profileId, this, this)
        recyclerView.adapter = postsAdapter


        //add a post
        val floatingBtn: FloatingActionButton = view.findViewById(R.id.floating_btn)
        floatingBtn.setOnClickListener {
            val intent = Intent(requireActivity(),AddPostActivity::class.java)

            intent.putExtra("profile_id", profileId)



            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout.setOnRefreshListener {
            db = AppDatabase.getDatabase(requireContext())
            DataFetcher.fetchDataAndStore(requireContext(),db) {
                fetchPosts()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        db = AppDatabase.getDatabase(requireContext())
        DataFetcher.fetchDataAndStore(requireContext(),db) {
            fetchPosts()
            swipeRefreshLayout.isRefreshing = false
        }
    }




    private fun fetchPosts() {
        loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetroFitClient.apiService.getAllPosts().execute()
                }

                if (response.isSuccessful) {
                    val postsNonSorted = response.body()
                    postsNonSorted?.let {

                        // Sort and update UI
                        posts = sortPostsByDateTime(postsNonSorted).toMutableList()
                        postsAdapter.updatePosts(posts)

                        // Save posts to Room database
                        withContext(Dispatchers.IO) {
                            db.postDao().insertAll(it)
                        }

                        // Logging
                        for (post in posts) {
                            Log.d(TAG, "Post: ${post.id}, Description: ${post.desc}, Date: ${post.date}")
                        }
                    }
                } else {
                    Log.e(TAG, "Retrofit request failed: ${response.code()}")
                    // Fetch posts from Room database
                    val roomPosts = withContext(Dispatchers.IO) {
                        db.postDao().getAllPosts()
                    }
                    // Sort and update UI
                    val sortedRoomPosts = sortPostsByDateTime(roomPosts).toMutableList()
                    postsAdapter.updatePosts(sortedRoomPosts)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching posts", e)
                // Fetch posts from Room database on exception
                posts = withContext(Dispatchers.IO) {
                    db.postDao().getAllPosts().toMutableList()
                }
                // Sort and update UI
                val sortedRoomPosts = sortPostsByDateTime(posts).toMutableList()
                postsAdapter.updatePosts(sortedRoomPosts)
            }
            finally {

                loading.visibility = View.GONE

                frame.setBackgroundResource(R.color.polls_bg)
            }
        }
    }




    private fun sortPostsByDateTime(posts: List<Post>): List<Post> {
        val dateFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        return posts.sortedByDescending { post ->
            val localDate = LocalDate.parse(post.date, dateFormatter)
            val localTime = LocalTime.parse(post.time, timeFormatter)
            LocalDateTime.of(localDate, localTime)
        }
    }


    override fun onButtonEditClick(position: Int) {
        // Handle edit button click
    }

    override fun onButtonSaveClick(position: Int, post:Post) {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Month starts from 0
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val formattedDate = "$year-$month-$day"
        posts[position].date = formattedDate


        // Get the current time
        val currentTime = Date()

        // Define the formatter with the desired pattern
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        posts[position].time = formatter.format(currentTime)

        updatePost(posts[position].id,position)

    }



    override fun onButtonDeleteClick(position: Int) {
        showDeleteConfirmationDialog(position)

    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Are you sure you want to delete this post?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val post = posts[position]
                fetchCommentsByPostId(post.id)
                deletePost(posts[position].id,position)

            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun fetchCommentsByPostId(postId: String) {
        loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetroFitClient.apiService.getCommentsByPostId(postId)
                }

                if (response.isSuccessful) {
                    val comments = response.body()
                    comments?.let { commentList ->
                        // Handle comments retrieval
                        // Update UI or perform operations as needed
                        for (comment in commentList) {
                            val deleteResponse = withContext(Dispatchers.IO) {
                                RetroFitClient.apiService.deleteCommentById(comment.id)
                            }

                            if (deleteResponse.isSuccessful) {
                                Log.d(TAG, "Comment ${comment.id} deleted successfully")
                                // Optionally: Update UI or perform operations after successful deletion
                            } else {
                                Log.e(TAG, "Failed to delete comment ${comment.id}: ${deleteResponse.code()}")
                                // Handle failure scenario
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Retrofit request failed: ${response.code()}")
                    // Handle failure scenario
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comments", e)
                // Handle exception scenario
            } finally {
                loading.visibility = View.GONE
            }
        }
    }


    override fun onItemClick(postId: String) {
        openDetailsActivity(postId)
    }

    private fun openDetailsActivity(postId: String) {
        var position: Int = -1
        var postAuthor:String =""
        var commentsIds: List<String> = emptyList()





        for(post in posts){
            if(post.id == postId){
                position = posts.indexOf(post)
                postAuthor = post.authorId
                commentsIds = post.comments
            }
        }

        if( postAuthor!=profileId) {
            posts[position].views++
            updatePost(postId, position)
        }
        val intent = Intent(requireActivity(), DetailsActivity::class.java)
        intent.putExtra("profile_id", profileId)

        intent.putExtra("comments_nb",nextCommentId.toString())
        intent.putStringArrayListExtra("comment_ids", ArrayList(commentsIds))
        intent.putExtra("post_id", postId.toString())
        startActivity(intent)
    }

    private fun deletePost(postId: String,position: Int) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.deletePostDb(postId)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Handle successful deletion
                    lifecycleScope.launch {
                        //deletePostFromApp(postId)
                        //posts.removeAt(position)  // Remove the post from the list

                        postsAdapter.notifyItemRemoved(position)  // Notify the adapter of the removed item
                    }
                    println("Post deleted successfully")
                } else {
                    // Handle unsuccessful response
                    println("Failed to delete post: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Handle failure
                println("Error: ${t.message}")
            }
        })

    }






    private fun updatePost(postId: String,position: Int) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.updatePost(postId,posts[position])
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Handle successful update
                    lifecycleScope.launch {
                        updatePostInApp(posts[position].id)
                        //postsAdapter.updatePosts(posts)
                        postsAdapter.notifyItemChanged(position)
                    }
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

    private suspend fun updatePostInApp(postId: String){
        withContext(Dispatchers.IO) {
            db.postDao().insertAll(posts)  // m ken aam yeemal update la eeml reload lal app fa zedta
            val postDao = db.postDao()
            val post = postDao.getPostById(postId)
            if (post != null) {
                postDao.updatePost(post)
                println("Post updated successfully in local database")
            } else {
                println("Post not found in local database")
            }
        }
    }


}
