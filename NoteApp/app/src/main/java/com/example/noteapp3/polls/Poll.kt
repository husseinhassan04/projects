package com.example.noteapp3.polls

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "poll")
class Poll {
    @PrimaryKey
    @SerializedName("id")
    var id: String = UUID.randomUUID().toString()

    @SerializedName("authorId")
    var authorId: String = ""

    @SerializedName("text")
    var text: String = ""

    @SerializedName("option1")
    var option1: String = ""

    @SerializedName("selected1")
    var selected1: MutableList<String> = mutableListOf()

    @SerializedName("option2")
    var option2: String = ""

    @SerializedName("selected2")
    var selected2: MutableList<String> = mutableListOf()

    @SerializedName("option3")
    var option3: String = ""

    @SerializedName("selected3")
    var selected3: MutableList<String> = mutableListOf()

    @SerializedName("option4")
    var option4: String = ""

    @SerializedName("selected4")
    var selected4: MutableList<String> = mutableListOf()
}

