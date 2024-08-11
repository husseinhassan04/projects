package com.example.noteapp3

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp3.messages.Message
import com.example.noteapp3.messages.MessageAdapter
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.ListenerRegistration
import com.jakewharton.threetenabp.AndroidThreeTen
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors

class MessagesPage : AppCompatActivity() {



    private lateinit var backButton: ImageButton
    private lateinit var button: ImageButton
    private lateinit var editText: EditText
    private lateinit var username: TextView
    private lateinit var onlineStatus: TextView
    private lateinit var profilePic: CircleImageView

    private lateinit var senderId: String
    private lateinit var receiverId: String
    private lateinit var receiverName: String
    private lateinit var senderUsername: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var sessionManager: SessionManager
    private val db = Firebase.firestore
    private lateinit var sound: MediaPlayer
    private var isInitialLoad = true
    private var allMessages = mutableListOf<Message>()
    private var messageListenerRegistration: ListenerRegistration? = null

    private var userStatusListener: ListenerRegistration? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_page)


        AndroidThreeTen.init(this)


        //users info
        senderId = intent.getStringExtra("senderId") ?: "-1"
        receiverId = intent.getStringExtra("receiverId") ?: "-1"
        receiverName = intent.getStringExtra("username") ?: "Unknown"

        username = findViewById(R.id.receiverName)
        username.text = receiverName
        profilePic = findViewById(R.id.profile_picture)
        onlineStatus = findViewById(R.id.online_status)

        getPicture(receiverId)


        val app = application as User
        val user = app.getUserDetails()
        senderUsername = user[SessionManager.KEY_USER_NAME]?:"Unknown"

        if(senderId=="-1"){
            senderId = user[SessionManager.KEY_USER_ID]?: "-1"
        }


        //layout
        backButton = findViewById(R.id.back)
        button = findViewById(R.id.button)
        editText = findViewById(R.id.editTextMessage)
        editText.requestFocus()
        recyclerView = findViewById(R.id.recyclerViewMessages)


        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No need to implement
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    button.setImageResource(R.drawable.send_message)
                } else {
                    button.setImageResource(R.drawable.send_message_colored)
                }}
        })


        sessionManager = SessionManager(this)

        if (senderId == "-1" || receiverId == "-1") {
            Toast.makeText(this,"couldn't get id",Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        messageAdapter = MessageAdapter(senderId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        //message sound on send and receive
        sound = MediaPlayer.create(this,R.raw.message_sent)

        button.setOnClickListener {
            sendMessage()
        }

        loadMessages()

        startOnlineStatusListener()


        backButton.setOnClickListener{
            finish()
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListenerRegistration?.remove()
    }

    override fun onResume() {
        super.onResume()
        markMessagesAsRead()
        setUserInMessageActivity(true)

    }

    override fun onPause() {
        super.onPause()
        setUserInMessageActivity(false)
    }

    private fun startOnlineStatusListener() {
        userStatusListener = db.collection("users").document(receiverId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val isOnline = snapshot.getBoolean("isInMessageActivity") ?: false
                    // Update UI based on the online status
                    if(isOnline){
                        onlineStatus.visibility = View.VISIBLE
                    }
                    else{
                        onlineStatus.visibility = View.GONE
                    }

                }
            }
    }

    private fun setUserInMessageActivity(isInActivity: Boolean) {
        val userRef = db.collection("users").document(senderId)
        userRef.update("isInMessageActivity", isInActivity)
    }

    private fun shouldSendNotification(): Boolean {
        if (allMessages.isNotEmpty()) {
            val lastMessage = allMessages.last()
            // Notify if last message is sent by the current user and is not read
            return lastMessage.senderId == senderId && !lastMessage.read
        }
        return true // Default to true if there are no messages
    }

    private fun markMessagesAsRead() {
        val messagesRef = db.collection("messages")

        messagesRef
            .whereEqualTo("recipientId", senderId)
            .whereEqualTo("senderId", receiverId)
            .whereEqualTo("read", false) // Only update messages that are not read
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (document in querySnapshot.documents) {
                    val documentRef = messagesRef.document(document.id)
                    batch.update(documentRef, "read", true)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "All messages marked as read")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error marking messages as read", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching messages to mark as read", e)
            }
    }



    private fun sendMessage() {
        val messagesRef = db.collection("messages")
        val messageContent = editText.text.toString()

        if (messageContent.isBlank()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val messageData = hashMapOf(
            "senderId" to senderId,
            "recipientId" to receiverId,
            "timestamp" to FieldValue.serverTimestamp(),
            "content" to messageContent,
            "read" to false
        )

        messagesRef.add(messageData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Message added with ID: ${documentReference.id}")
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                editText.text.clear()

                // Send notification directly to user
                sendNotificationToUser(receiverId, messageContent, senderId)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding message", e)
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotificationToUser(receiverId: String, content: String, senderId: String) {
        if (!shouldSendNotification()) {
            return
        }
        val userRef = db.collection("users").document(receiverId)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            val token = documentSnapshot.getString("token")
            val isInMessageActivity = documentSnapshot.getBoolean("isInMessageActivity") ?: false
            if (token != null && !isInMessageActivity) {
                // Fetch the sender's username
                val senderRef = db.collection("users").document(senderId)
                senderRef.get().addOnSuccessListener { senderSnapshot ->
                    val json = JSONObject()
                    try {
                        val message = JSONObject().apply {
                            put("token", token)
                            put("data", JSONObject().apply {
                                put("title", "New Message from $senderUsername")
                                put("body", content)
                                put("senderId", senderId)
                                put("senderUsername", senderUsername)
                            })
                        }
                        json.put("message", message)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    Log.d(TAG, "JSON payload: $json")

                    Executors.newSingleThreadExecutor().execute {
                        try {
                            val credentialsStream: InputStream = assets.open("service-account.json")
                            val credentials = GoogleCredentials.fromStream(credentialsStream)
                                .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                            credentials.refreshIfExpired()
                            val accessToken = credentials.accessToken.tokenValue

                            val url = URL("https://fcm.googleapis.com/v1/projects/noteapp3-99f19/messages:send")
                            val client = OkHttpClient()
                            val requestBody = RequestBody.create(
                                "application/json; charset=utf-8".toMediaType(),
                                json.toString()
                            )
                            val request = Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .addHeader("Authorization", "Bearer $accessToken")
                                .build()

                            client.newCall(request).execute().use { response ->
                                if (response.isSuccessful) {
                                    Log.d(TAG, "Notification sent successfully.")
                                } else {
                                    Log.w(TAG, "Error sending notification. Response code: ${response.code}")
                                    Log.w(TAG, "Response body: ${response.body?.string()}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception while sending notification", e)
                        }
                    }
                }
            }
        }
    }







    private fun loadMessages() {
        val messagesRef = db.collection("messages")
        messageListenerRegistration = messagesRef
            .whereIn("recipientId", listOf(senderId, receiverId))
            .whereIn("senderId", listOf(senderId, receiverId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = mutableListOf<Message>()
                    for (document in snapshot.documents) {
                        val senderId = document.getString("senderId") ?: ""
                        val recipientId = document.getString("recipientId") ?: ""
                        val content = document.getString("content") ?: ""
                        val read = document.getBoolean("read") ?: false
                        val timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                        // Ensure only messages between the specified sender and receiver are included
                        if ((senderId == this.senderId && recipientId == this.receiverId) ||
                            (senderId == this.receiverId && recipientId == this.senderId)) {
                            val message = Message(content, senderId, recipientId, timestamp, read)
                            messages.add(message)
                        }
                    }
                    allMessages.clear()
                    allMessages.addAll(messages)
                    // Update your RecyclerView with the messages list
                    messageAdapter.submitList(messages) {
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                    if (!isInitialLoad) {
                        sound.start()
                    } else {
                        isInitialLoad = false
                    }
                    Log.d(TAG, "Messages loaded: ${messages.size}")
                } else {
                    Log.d(TAG, "No messages found")
                }
            }
    }

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

    private fun getPicture(profileId: String) {
        val apiService = RetroFitClient.apiService
        val call = apiService.getAllProfiles()

        call.enqueue(object : Callback<List<profile>> {
            override fun onResponse(call: Call<List<profile>>, response: Response<List<profile>>) {
                if (response.isSuccessful) {
                    val profiles = response.body()
                    profiles?.let { profileList ->
                        val profile = profileList.find { it.id == profileId }
                        profile?.let {
                            val bitmap = decodeBase64(it.picture)
                            bitmap?.let { decodedBitmap ->
                                // Assuming profilePic is an ImageView
                                profilePic.setImageBitmap(decodedBitmap)
                            } ?: run {
                                Log.e(TAG, "Failed to decode Base64 string for profile picture")
                            }
                        } ?: run {
                            Log.e(TAG, "Profile with ID $profileId not found")
                        }
                    } ?: run {
                        Log.e(TAG, "Empty response body")
                    }
                } else {
                    Log.e(TAG, "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<profile>>, t: Throwable) {
                Log.e(TAG, "Error fetching profiles", t)
            }
        })
    }




    companion object {
        private const val TAG = "MessagesPage"
    }
}
