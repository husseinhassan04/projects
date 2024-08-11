package com.example.noteapp3.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName="comments")
class Comment {
    @PrimaryKey
    @SerializedName("id")
    var id: String = UUID.randomUUID().toString()
    @SerializedName("postId")
    var postId: String = ""
    @SerializedName("text")
    var text: String =""
    @SerializedName("profileId")
    var profileId: String=""

}