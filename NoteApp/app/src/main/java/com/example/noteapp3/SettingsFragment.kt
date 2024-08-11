package com.example.noteapp3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat


class SettingsFragment : Fragment() {
    private lateinit var notificationSwitch : SwitchCompat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


        notificationSwitch = view.findViewById(R.id.notification_state)

        val sessionManager = SessionManager(requireContext())

        var isNotificationEnabled:Boolean = sessionManager.areNotificationsEnabled()

        if(isNotificationEnabled){
            notificationSwitch.isChecked = true
        }
        else{
            notificationSwitch.isChecked = false
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sessionManager.setNotificationsEnabled(true)
            } else {
                sessionManager.setNotificationsEnabled(false)
            }
        }


        return view
    }


}