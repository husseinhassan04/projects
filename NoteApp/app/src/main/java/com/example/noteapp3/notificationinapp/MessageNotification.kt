package com.example.noteapp3.notificationinapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_notifications")
data class MessageNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var senderPicture: String,
    val senderId: String,
    val senderName:String,
    val messageContent:String,
)
