package com.example.noteapp3.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp3.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MessageAdapter(private val currentUserId: String) :
    ListAdapter<Message, MessageAdapter.MessageViewHolder>(DiffCallback()) {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val day: TextView = itemView.findViewById(R.id.day)
        val read: TextView = itemView.findViewById(R.id.read)

        var lastMessageTimestamp: Long? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutRes = if (viewType == VIEW_TYPE_SENDER) {
            R.layout.item_message_sender // Layout for sender's message

        } else {
            R.layout.item_message_receiver // Layout for receiver's message
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)

        // Set message text and time
        holder.messageText.text = message.messageText
        holder.messageTime.text = timestampToString(message.timestamp)

        if(message.read){
            holder.read.visibility = View.VISIBLE
        }

        // Show or hide day TextView based on message date
        val currentDateTime = Calendar.getInstance()
        val messageDateTime = Calendar.getInstance().apply { timeInMillis = message.timestamp }

        if (position == 0) {
            holder.day.visibility = View.VISIBLE
            holder.day.text = getDayText(messageDateTime, currentDateTime)
        } else {
            val previousMessage = getItem(position - 1)
            val previousMessageDateTime = Calendar.getInstance().apply { timeInMillis = previousMessage.timestamp }

            if (!isSameDay(messageDateTime, previousMessageDateTime)) {
                holder.day.visibility = View.VISIBLE
                holder.day.text = getDayText(messageDateTime, currentDateTime)
            } else {
                holder.day.visibility = View.GONE
            }
        }

        // Adjust message appearance based on sender or receiver
        val isSender = message.senderId.toString() == currentUserId
        if (isSender) {
            holder.messageContainer.setBackgroundResource(R.drawable.sender_message_background)
        } else {
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message)
            holder.read.visibility = View.GONE
        }

        // Update last message timestamp for next comparison
        holder.lastMessageTimestamp = message.timestamp
    }


    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId.toString() == currentUserId) {
            VIEW_TYPE_SENDER
        } else {
            VIEW_TYPE_RECEIVER
        }
    }


    private fun timestampToString(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getDayText(messageDate: Calendar, currentDate: Calendar): String {
        return when {
            isSameDay(messageDate, currentDate) -> "Today"
            isSameDay(messageDate.apply { add(Calendar.DAY_OF_YEAR, 1) }, currentDate) -> "Yesterday"
            else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(messageDate.time)
        }
    }




    companion object {
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVER = 2
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
