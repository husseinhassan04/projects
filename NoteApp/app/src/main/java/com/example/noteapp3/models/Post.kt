package com.example.noteapp3.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.noteapp3.models.Comment
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "posts")
class Post {

    @PrimaryKey
    @SerializedName("id")
    var id: String= UUID.randomUUID().toString()
    @SerializedName("title")
    var title: String =""
    @SerializedName("views")
    var views: Int=0
    @SerializedName("description")
    var desc: String =""
    @SerializedName("date")
    var date: String=""
    @SerializedName("time")
    var time: String=""
    @SerializedName("comments")
    @TypeConverters(StringListConverter::class)
    var comments: MutableList<String> = mutableListOf()
    @SerializedName("authorId")
    var authorId: String =""
    @SerializedName("img")
    @TypeConverters(StringListConverter::class)
    var imgBase64: List<String> = emptyList()
    @SerializedName("x")
    var x: Double = 0.00
    @SerializedName("y")
    var y: Double = 0.00

}