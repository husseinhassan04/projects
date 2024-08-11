package com.example.noteapp3.messages

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.noteapp3.MessagesPage
import com.example.noteapp3.R
import com.example.noteapp3.SessionManager
import com.example.noteapp3.User
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import com.example.noteapp3.notificationinapp.MessageNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val channelId = "channel_id"
const val channelGroup = "NoteApp"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var senderIdMain: String = ""
    private var base64Profile: String = ""
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()
        Log.d("FCM", "Message received from: ${remoteMessage.from}")
        val data = remoteMessage.data
        val title = data["title"]
        val body = data["body"]
        val senderId = data["senderId"]
        val senderUsername = data["senderUsername"]
        if (title != null && body != null && !token.isNullOrEmpty()) {
            sendNotification(title, body, senderId, senderUsername)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?, senderId: String?,senderUsername: String?) {
        senderIdMain = senderId?:"-1"


        val sessionManager = SessionManager(this)
        val isNotificationEnabled = sessionManager.areNotificationsEnabled()


        val notification = MessageNotification(
            senderPicture = "",
            senderId = senderId?:"-1",
            senderName = senderUsername?:"unknown",
            messageContent = messageBody?:"message"
        )



        if (isNotificationEnabled) {

            loadProfile(notification)

            Log.d("FCM", "Preparing to send notification with title: $title and body: $messageBody")
            val intent = Intent(this, MessagesPage::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(
                    "receiverId",
                    senderId
                ) //sender of the notification is the receiver of the user who opens the notification
                putExtra("username", senderUsername)
            }


            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            // Create custom RemoteViews
//            val remoteViews = getRemoteView(title, messageBody)

            val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContent(remoteViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelGroup,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(
                System.currentTimeMillis().toInt(),
                notificationBuilder.build()
            )
        }
    }

    private fun loadProfile(notification: MessageNotification) {
        val sessionManager = SessionManager(this)
        val user = sessionManager.getUserDetails()
        val id = senderIdMain
        val apiService = RetroFitClient.apiService

        if (id != null) {
            val call = apiService.getProfileById(id)
            call.enqueue(object : Callback<profile> {
                override fun onResponse(call: Call<profile>, response: Response<profile>) {
                    if (response.isSuccessful) {
                        val userProfile = response.body()!!
                        // Update UI with the loaded profile
                        if(userProfile.picture!="") {
                            base64Profile = userProfile.picture
                        }

                        if(base64Profile!=""){
                            notification.senderPicture = base64Profile
                        }

                        val db = AppDatabase.getDatabase(this@MyFirebaseMessagingService)
                        val notificationDao = db.notificationDao()
                        CoroutineScope(Dispatchers.IO).launch {
                            notificationDao.insert(notification)
                        }
                    }
                }
                override fun onFailure(call: Call<profile>, t: Throwable) {
                }
            })
        }
    }




}
