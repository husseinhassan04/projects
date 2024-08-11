package com.example.noteapp3.notificationinapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp3.MessagesPage
import com.example.noteapp3.R

class NotificationAdapter(private val notifications: List<MessageNotification>
                          , private val context: Context
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_inapp_notification, parent, false)
        return NotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val currentNotification = notifications[position]
        holder.senderInfo.text = "New message from ${currentNotification.senderName}"
        holder.messageDetails.text = currentNotification.messageContent
        holder.senderPicture.setImageBitmap(decodeBase64(currentNotification.senderPicture))

        holder.itemView.setOnClickListener {
            // Start MessagePage activity
            val intent = Intent(context, MessagesPage::class.java).apply {
                putExtra("receiverId", currentNotification.senderId)
                putExtra("username",currentNotification.senderName)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = notifications.size

    private fun decodeBase64(base64Str: String): Bitmap? {
        return try {
            if (base64Str.isNotEmpty()) {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            Log.e(ContentValues.TAG, "Error decoding Base64 string", e)
            null
        }
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderPicture: ImageView = itemView.findViewById(R.id.sender_picture)
        val senderInfo: TextView = itemView.findViewById(R.id.sender_info)
        val messageDetails: TextView = itemView.findViewById(R.id.message_details)
    }
}
