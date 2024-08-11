package com.example.noteapp3.messages

data class Message(
    val messageText: String,
    val senderId: String,
    val recipientId: String,
    val timestamp: Long,
    val read: Boolean
)