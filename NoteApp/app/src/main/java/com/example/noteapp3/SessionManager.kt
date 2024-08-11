package com.example.noteapp3

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.noteapp3.models.AppDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val db = Firebase.firestore

    fun createLoginSession(userId: String, userName: String, picture: String) {
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USER_PICTURE, picture)
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - loginTime) < SESSION_DURATION
    }

    fun getUserDetails(): HashMap<String, String> {
        val user: HashMap<String, String> = HashMap()
        user[KEY_USER_ID] = sharedPreferences.getString(KEY_USER_ID, null).toString()
        user[KEY_USER_NAME] = sharedPreferences.getString(KEY_USER_NAME, null).toString()
        user[KEY_USER_PICTURE] = sharedPreferences.getString(KEY_USER_PICTURE, null).toString()
        return user
    }

    fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(USER_TOKEN, null)
    }

    fun logoutUser() {
        val db = AppDatabase.getDatabase(context)
        val notificationDao = db.notificationDao()
        CoroutineScope(Dispatchers.IO).launch {
            notificationDao.deleteAllNotifications()
        }
        editor.clear()
        editor.apply()
    }


    fun changeUserPicture(picture:String){
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_PICTURE,picture)
        editor.apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        editor.putBoolean(KEY_NOTIFICATIONS, enabled)
        editor.commit()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true) // Default to true if not set
    }







    companion object {
        const val PREF_NAME = "user_session"
        private const val USER_TOKEN = "user_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "name"
        const val KEY_USER_PICTURE = "picture"
        const val KEY_LOGIN_TIME = "login_time"
        const val KEY_NOTIFICATIONS = "notificationsEnabled"
        const val SESSION_DURATION = 24*60 * 60 * 1000*10 // 24 hours *10 = 10 days
    }
}
