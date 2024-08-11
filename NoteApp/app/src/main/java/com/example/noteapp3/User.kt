package com.example.noteapp3

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

class User: Application() {
    companion object {
        private lateinit var instance: User

        fun getInstance(): User {
            return instance
        }
    }

    var isAppInForeground = false
        private set

    private lateinit var sessionManager : SessionManager
    private lateinit var user : HashMap<String, String>
    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        user = sessionManager.getUserDetails()

        instance = this


    }
    fun getUserDetails(): HashMap<String, String> {
        return user
    }
}