package com.example.noteapp3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp3.models.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        fetchData()



    }




    private fun fetchData() {
        DataFetcher.fetchDataAndStore(this, db) {
            loadNextActivity()
        }
    }

    private fun loadNextActivity() {
        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this@MainActivity, LoginPage::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this@MainActivity, Feed::class.java)
            startActivity(intent)
            finish()
        }
    }
}
