package com.example.kitabi2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the application instance and check for books
        val app = application as KitabiApplication
        if (app.booksList.isNotEmpty()) {
            navigateToFeed()
        } else {
            // Optionally, you can show a loading indicator here
            app.fetchBooksFromFirestore { booksList ->
                if (booksList.isNotEmpty()) {
                    navigateToFeed()
                } else {
                    Toast.makeText(this, "Connection Error\nTry reloading app", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToFeed() {
        val intent = Intent(this, FeedActivity::class.java)
        startActivity(intent)
        finish()
    }
}
