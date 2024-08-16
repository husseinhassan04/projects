package com.example.noteapp3.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "stream_url")
class StreamUrl{
    @PrimaryKey
    @SerializedName("id")
    var id= UUID.randomUUID().toString()


    @SerializedName("url")
    var url=""

}
