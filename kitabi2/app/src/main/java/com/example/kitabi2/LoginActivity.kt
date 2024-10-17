package com.example.kitabi2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.kitabi2.database.Profile
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import kotlin.math.sign

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var pass: EditText
    private lateinit var loginBtn: Button
    private lateinit var signUp: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email)
        pass = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginButton)
        signUp = findViewById(R.id.sign_up)
        loginBtn.setOnClickListener {
            login(email.text.toString(), pass.text.toString())
        }

        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // SHA-1 hash function
    fun sha1(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(bytes)

        return digest.joinToString("") {
            "%02x".format(it)
        }
    }

    // Fetch profiles from Firestore
    private fun fetchProfilesFromFirestore(onProfilesFetched: (List<Profile>) -> Unit) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection("profiles")
            .get()
            .addOnSuccessListener { documents ->
                val profiles = mutableListOf<Profile>()
                for (document in documents) {
                    val profile = document.toObject(Profile::class.java)
                    profiles.add(profile)
                }
                onProfilesFetched(profiles) // Trigger callback after fetching profiles
            }
            .addOnFailureListener { e ->
                println("Error fetching profiles: $e")
            }
    }

    // Perform login with the fetched profiles
    private fun login(email: String, pass: String) {
        fetchProfilesFromFirestore { profiles ->
            var isLoginSuccessful = false

            for (profile in profiles) {
                if (email.lowercase() == profile.email.lowercase()) {
                    if (sha1(pass) == profile.password) {
                        val sessionManager = SessionManager(this)
                        sessionManager.createLoginSession(profile.id,profile.username,profile.url)
                        isLoginSuccessful = true
                        finish()
                        break
                    }
                }
            }

            if (!isLoginSuccessful) {
                Toast.makeText(this, "Wrong Credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
