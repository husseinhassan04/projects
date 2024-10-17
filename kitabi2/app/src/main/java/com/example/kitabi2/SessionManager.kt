package com.example.kitabi2


import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
        if(KEY_USER_ID.isBlank()){
            return false
        }
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



    fun logoutUser() {
        editor.clear()
        editor.apply()
    }

    fun changePicture(picture: String) {
        editor.putString(KEY_USER_PICTURE, picture)
        editor.apply()
    }

    fun getUserProfilePic(): String {
        return sharedPreferences.getString(KEY_USER_PICTURE, null).toString()
    }


    companion object {
        const val PREF_NAME = "user_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "name"
        const val KEY_USER_PICTURE = "picture"
        const val KEY_LOGIN_TIME = "login_time"
        const val SESSION_DURATION = 24*60 * 60 * 1000*10 // 24 hours *10 = 10 days
    }
}
