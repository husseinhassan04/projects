package com.example.noteapp3.models

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetroFitClient {

    private const val BASE_URL = "http://192.168.180.28:3000/"
//    private const val BASE_URL = "http://192.168.1.9:3000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build()


    //to convert int to string (**IMP**)
//    private val gson: Gson = GsonBuilder()
//        .registerTypeAdapter(Int::class.javaObjectType, PostIdAdapter()) // Ensure both primitive and boxed types are handled
//        .registerTypeAdapter(Int::class.javaPrimitiveType, PostIdAdapter())
//        .create()

    //  .addConverterFactory(GsonConverterFactory.create(gson))

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}