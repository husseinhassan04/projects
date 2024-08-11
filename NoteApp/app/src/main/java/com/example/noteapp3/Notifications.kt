package com.example.noteapp3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp3.messages.MessageAdapter
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import com.example.noteapp3.notificationinapp.MessageNotification
import com.example.noteapp3.notificationinapp.NotificationAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Notifications : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: NotificationAdapter
    private lateinit var notificationList: List<MessageNotification>
    private var userPictureMap: Map<String, String> = emptyMap()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)


        val db = AppDatabase.getDatabase(requireContext())
        val notificationDao = db.notificationDao()

        recyclerView = view.findViewById(R.id.notifications_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {

            notificationList = notificationDao.getAllNotifications()

            notificationList = notificationList.asReversed()


            messageAdapter = NotificationAdapter(notificationList,requireContext())
            recyclerView.adapter = messageAdapter


        }

        return view
    }

}