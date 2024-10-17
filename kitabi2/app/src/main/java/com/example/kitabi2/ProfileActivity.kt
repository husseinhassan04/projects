package com.example.kitabi2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.kitabi2.roomdb.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: CircleImageView
    private lateinit var changePictureBtn: CircleImageView
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private lateinit var backButton: Button
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private var userId: String? = null
    private var currentImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        val user = sessionManager.getUserDetails()
        if (user == null) {
            finish()
            return
        } else {
            userId = user[SessionManager.KEY_USER_ID] ?: ""
        }

        setContentView(R.layout.activity_profile)

        // Initialize views
        profileImageView = findViewById(R.id.profile_image)
        changePasswordButton = findViewById(R.id.change_password_button)
        changePictureBtn = findViewById(R.id.edit_icon)
        logoutButton = findViewById(R.id.logout_button)
        backButton = findViewById(R.id.back_button)

        // Initialize Firebase Firestore and Storage
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load user profile
        loadUserProfile()

        // Set click listener for changing the password
        changePasswordButton.setOnClickListener {
            val intent = Intent(this,ChangePasswordActivity::class.java)
            intent.putExtra("userId",userId)
            startActivity(intent)
        }

        // Set click listener for logout button
        logoutButton.setOnClickListener {
            sessionManager.logoutUser()
            val db = AppDatabase.getDatabase(this)

            lifecycleScope.launch(Dispatchers.IO) {
                sessionManager.logoutUser()
                db.clearAllTables()  // Perform database operation on IO thread
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }

        // Set click listener to change the profile picture
        changePictureBtn.setOnClickListener {
            pickImageFromCamera()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    // Function to load user profile from Firestore
    private fun loadUserProfile() {
        userId?.let { uid ->
            firestore.collection("profiles")
                .whereEqualTo("id", uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        currentImageUrl = documentSnapshot.getString("url")
                        if (currentImageUrl != null) {
                            // Load image using Glide
                            Glide.with(this)
                                .load(currentImageUrl)
                                .into(profileImageView)
                        } else {
                            Toast.makeText(this, "No profile picture found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User profile does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to pick image from camera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            profileImageView.setImageBitmap(imageBitmap)
            uploadImageToFirebase(imageBitmap)
        }
    }

    // Launch camera to take picture
    private fun pickImageFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    // Function to upload the image to Firebase Storage
    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        userId?.let { uid ->
            val storageRef = storage.reference.child("profilePictures/$uid.jpg")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = storageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                // Get the download URL and update Firestore
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateProfileImageUrl(uri.toString())
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to update profile image URL in Firestore
    private fun updateProfileImageUrl(newImageUrl: String) {
        userId?.let { uid ->
            firestore.collection("profiles")
                .whereEqualTo("id", uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        val profileRef = documentSnapshot.reference

                        profileRef.update("url", newImageUrl)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                                sessionManager.changePicture(newImageUrl)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }
}
