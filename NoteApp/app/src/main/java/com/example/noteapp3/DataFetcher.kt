package com.example.noteapp3

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.Comment
import com.example.noteapp3.models.Post
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.StreamUrl
import com.example.noteapp3.polls.Poll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object DataFetcher {

    private const val TAG = "DataFetcher"

    fun fetchDataAndStore(context: Context, db: AppDatabase, callback: () -> Unit) {
        val apiService = RetroFitClient.apiService

        // Fetch and store comments
        apiService.getAllComments().enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val comments = response.body()
                    comments?.let {
                        (context as? AppCompatActivity)?.lifecycleScope?.launch {
                            saveCommentsToDatabase(db, it)
                            removeCommentsNotInResponse(db, it)
                            checkAndProceed(context, db, callback)
                        }
                    }
                } else {
                    Log.e(TAG, "Response not successful")

                    checkAndProceed(context, db, callback)
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.e(TAG, "Error fetching comments", t)
                checkAndProceed(context, db, callback)
            }
        })

        // Fetch and store posts
        apiService.getAllPosts().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val posts = response.body()
                    posts?.let {
                        (context as? AppCompatActivity)?.lifecycleScope?.launch {
                            savePostsToDatabase(db, it)
                            removePostsNotInResponse(db, it)
                            checkAndProceed(context, db, callback)
                        }
                    }
                } else {
                    Log.e(TAG, "Response not successful")
                    checkAndProceed(context, db, callback)
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.e(TAG, "Error fetching posts", t)
                Toast.makeText(
                    context,
                    "Connection error.\nPlease check your connection",
                    Toast.LENGTH_SHORT
                ).show()
                checkAndProceed(context, db, callback)
            }
        })

        //fetch and store polls
        apiService.getAllPolls().enqueue(object : Callback<List<Poll>> {
            override fun onResponse(call: Call<List<Poll>>, response: Response<List<Poll>>) {
                if (response.isSuccessful) {
                    val polls = response.body()
                    polls?.let {
                        (context as? AppCompatActivity)?.lifecycleScope?.launch {
                            savePollsToDatabase(db, it)
                            removePollsNotInResponse(db, it)
                            checkAndProceed(context, db, callback)
                        }
                    }
                } else {
                    Log.e(TAG, "Response not successful")
                    checkAndProceed(context, db, callback)
                }
            }

            override fun onFailure(call: Call<List<Poll>>, t: Throwable) {
                Log.e(TAG, "Error fetching polls", t)
                Toast.makeText(
                    context,
                    "Connection error.\nPlease check your connection",
                    Toast.LENGTH_SHORT
                ).show()
                checkAndProceed(context, db, callback)
            }
        })

        //fetch and store live streams
        apiService.getAllLiveUrls().enqueue(object :Callback<List<StreamUrl>>{


            override fun onResponse(call: Call<List<StreamUrl>>, response: Response<List<StreamUrl>>) {
                if (response.isSuccessful) {
                    val urls = response.body()
                    urls?.let{
                        (context as? AppCompatActivity)?.lifecycleScope?.launch {
                            saveStreamUrlsToDatabase(db,it)
                            checkAndProceed(context, db, callback)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<StreamUrl>>, t: Throwable) {
                Log.e(TAG, "Error fetching stream urls", t)
                Toast.makeText(
                    context,
                    "Connection error.\nPlease check your connection",
                    Toast.LENGTH_SHORT
                ).show()
                checkAndProceed(context, db, callback)
            }

        }

        )
    }

    private suspend fun saveCommentsToDatabase(db: AppDatabase, comments: List<Comment>) {
        withContext(Dispatchers.IO) {
            db.commentDao().insertAll(comments)
            Log.d(TAG, "Comments saved to database")
        }
    }

    private suspend fun removeCommentsNotInResponse(db: AppDatabase, serverComments: List<Comment>) {
        withContext(Dispatchers.IO) {
            val dbComments = db.commentDao().getAllComments()
            val dbCommentIds = dbComments.map { it.id }
            val serverCommentIds = serverComments.map { it.id }

            // Find IDs present in the database but not in the server response
            val idsToRemove = dbCommentIds.filter { !serverCommentIds.contains(it) }

            // Remove comments from the database
            db.commentDao().deleteByIds(idsToRemove)
            Log.d(TAG, "Removed comments not present in server response")
        }
    }

    private suspend fun savePostsToDatabase(db: AppDatabase, posts: List<Post>) {
        withContext(Dispatchers.IO) {
            db.postDao().insertAll(posts)
            Log.d(TAG, "Posts saved to database")
        }
    }
    private suspend fun savePollsToDatabase(db: AppDatabase, polls: List<Poll>) {
        withContext(Dispatchers.IO) {
            db.pollDao().insertAll(polls)
            Log.d(TAG, "Posts saved to database")
        }
    }

    private suspend fun removePostsNotInResponse(db: AppDatabase, serverPosts: List<Post>) {
        withContext(Dispatchers.IO) {
            val dbPosts = db.postDao().getAllPosts()
            val dbPostIds = dbPosts.map { it.id }
            val serverPostIds = serverPosts.map { it.id }

            // Find IDs present in the database but not in the server response
            val idsToRemove = dbPostIds.filter { !serverPostIds.contains(it) }

            // Remove posts from the database
            db.postDao().deleteByIds(idsToRemove)
            Log.d(TAG, "Removed posts not present in server response")
        }
    }

    private suspend fun removePollsNotInResponse(db: AppDatabase, serverPosts: List<Poll>) {
        withContext(Dispatchers.IO) {
            val dbPolls = db.pollDao().getAllPolls()
            val dbPostIds = dbPolls.map { it.id }
            val serverPollsIds = serverPosts.map { it.id }

            // Find IDs present in the database but not in the server response
            val idsToRemove = dbPostIds.filter { !serverPollsIds.contains(it) }

            // Remove posts from the database
            db.pollDao().deleteByIds(idsToRemove)
            Log.d(TAG, "Removed posts not present in server response")
        }
    }

    private suspend fun saveStreamUrlsToDatabase(db: AppDatabase, urls: List<StreamUrl>) {
        withContext(Dispatchers.IO) {
            db.liveStreamUrlsDao().insertAll(urls)
            Log.d(TAG, "Posts saved to database")
        }
    }


    private fun checkAndProceed(context: Context, db: AppDatabase, callback: () -> Unit) {
        (context as? AppCompatActivity)?.lifecycleScope?.launch {
            val hasComments = withContext(Dispatchers.IO) { db.commentDao().getAllComments().isNotEmpty() }

            val hasPosts = withContext(Dispatchers.IO) { db.postDao().getAllPosts().isNotEmpty() }

            val hasPolls = withContext(Dispatchers.IO) { db.pollDao().getAllPolls().isNotEmpty() }

            if (hasComments && hasPosts && hasPolls) {
                callback()
            } else {
                Log.e(TAG, "No data available in local database")
                Toast.makeText(
                    context,
                    "No data available in local database.\n Please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
