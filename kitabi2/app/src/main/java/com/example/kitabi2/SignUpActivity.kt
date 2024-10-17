package com.example.kitabi2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kitabi2.database.Profile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.UUID

class SignUpActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button

    private val CAMERA_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_CODE = 101
    private val userId = UUID.randomUUID().toString()

    private lateinit var imageView: CircleImageView
    private lateinit var uploadButton: CircleImageView

    private var imageBitmap: Bitmap? = null // To store the captured image temporarily

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        usernameEditText = findViewById(R.id.username)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        signUpButton = findViewById(R.id.sign_up_button)

        signUpButton.setOnClickListener {
            handleSignUp()
        }

        imageView = findViewById(R.id.image)
        uploadButton = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            requestCameraPermission()
        }
    }

    private fun handleSignUp() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (username.isEmpty()) {
            showToast("Please enter your username")
            return
        }

        if (email.isEmpty()) {
            showToast("Please enter your email")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email")
            return
        }

        if (password.isEmpty()) {
            showToast("Please enter your password")
            return
        }

        if (password.length < 8) {
            showToast("Password should be at least 8 characters")
            return
        }

        if (confirmPassword != password) {
            showToast("Passwords do not match")
            return
        }

        if (imageBitmap == null) {
            showToast("Please upload a profile picture")
            return
        }

        // Hash the password
        val hashedPassword = sha1(password)

        // Upload the profile picture and handle user registration upon successful upload
        uploadImageToFirebase(imageBitmap!!, username, email, hashedPassword)
    }

    // SHA-1 hash function for password security
    private fun sha1(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            // Permission is already granted
            openCamera()
        }
    }

    // Handle the result of permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open the camera to capture an image
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    // Handle the result from the camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

    // Upload image to Firebase Storage and then save the user profile in Firestore
    private fun uploadImageToFirebase(imageBitmap: Bitmap, username: String, email: String, hashedPassword: String) {
        // Convert Bitmap to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()

        // Define the storage reference (path in Firebase Storage)
        val storageRef = FirebaseStorage.getInstance().reference.child("profilePictures/$userId.jpg")

        // Upload the image
        val uploadTask = storageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            // Get the download URL after upload
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Once the image is uploaded and URL is obtained, save the user profile
                saveUserProfileToFirestore(username, email, hashedPassword, uri.toString())
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseUpload", "Failed to upload image: ${e.message}")
            showToast("Failed to upload profile picture. Sign-up aborted.")
        }
    }

    // Save the user profile to Firestore including the image URL
    private fun saveUserProfileToFirestore(username: String, email: String, hashedPassword: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val profile = Profile().apply {
            id = userId
            this.username = username
            this.email = email
            this.password = hashedPassword
            this.url = imageUrl
        }

        db.collection("profiles").add(profile)
            .addOnSuccessListener {
                showToast("Sign Up Successful!")
                startActivity(Intent(this, FeedActivity::class.java))
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpload", "Failed to save profile: ${e.message}")
                showToast("Sign-up failed. Please try again.")
            }
    }
}
