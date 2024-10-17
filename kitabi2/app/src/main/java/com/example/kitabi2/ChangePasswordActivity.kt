package com.example.kitabi2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        userId = intent.getStringExtra("userId") ?: ""

        oldPasswordEditText = findViewById(R.id.current_password)
        newPasswordEditText = findViewById(R.id.new_password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        changePasswordButton = findViewById(R.id.change_password_button)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        changePasswordButton.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPassword = oldPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 8) {
            Toast.makeText(this, "New password should be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate old password in Firestore
        validateOldPasswordAndChange(userId, oldPassword, newPassword)
    }

    private fun validateOldPasswordAndChange(userId: String, oldPassword: String, newPassword: String) {
        firestore.collection("profiles").whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val document = documents.firstOrNull()
                val storedHashedPassword = document?.getString("password")

                if (storedHashedPassword == sha1(oldPassword)) {
                    // Update password if old password is correct
                    document.reference.update("password", sha1(newPassword))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                            finish()  // Close the activity after change
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to change password: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error retrieving user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sha1(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
