package com.example.noteapp3.models

import android.provider.ContactsContract
import com.example.noteapp3.models.Comment
import com.example.noteapp3.polls.Poll
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("posts")
    fun getAllPosts(): Call<List<Post>>

    @GET("comments")
    fun getAllComments(): Call<List<Comment>>


    @GET("comments")
    suspend fun getCommentsByPostId(@Query("postId") postId: String): Response<List<Comment>>

    @POST("comments")
    fun addComment(@Body comment: Comment): Call<Void>

    @DELETE("comments/{id}")
    suspend fun deleteCommentById(@Path("id") commentId: String): Response<Void>



    @GET("profile")
    fun getAllProfiles(): Call<List<profile>>

    @GET("profile/{id}")
    fun getProfileById(@Path("id") id: String): Call<profile>

    @PUT("profile/{id}")
    fun updateProfile(@Path("id")id:String,@Body updatedProfile:profile):Call<Void>

    @GET("poll")
    fun getAllPolls(): Call<List<Poll>>

    @PUT("poll/{id}")
    fun updatePoll(@Path("id") id: String, @Body updatedPoll: Poll): Call<Void>

    @POST("poll")
    fun addPoll(@Body poll: Poll): Call<Void>


    @POST("profile")
    fun createProfile(@Body profile: profile): Call<profile>


    @DELETE("posts/{postId}")
    fun deletePostDb(@Path("postId") postId: String): Call<Void>


    @PUT("posts/{postId}")
    fun updatePost(@Path("postId") postId: String, @Body updatedPost: Post): Call<Void>

    @POST("posts")
    fun addPost(@Body post: Post): Call<Void>

    @GET("liveStreams")
    fun getAllLiveUrls(): Call<List<StreamUrl>>

    @POST("liveStreams")
    fun addLiveUrl(@Body liveUrl: StreamUrl): Call<Void>

}