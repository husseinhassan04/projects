package com.example.noteapp3.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName= "profile")
class profile {
    @PrimaryKey
    @SerializedName("id")
    var id: String = UUID.randomUUID().toString()
    @SerializedName("name")
    var name:String=""
    @SerializedName("username")
    var user:String=""
    @SerializedName("password")
    var pass:String=""
    @SerializedName("picture")
    var picture:String=""
    @SerializedName("birthDate")
    var dateOfBirth:String=""
}