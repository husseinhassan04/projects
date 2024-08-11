package com.example.noteapp3

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {



    private lateinit var profilePic:CircleImageView
    private lateinit var editPictureButton : ImageButton
    private lateinit var deleteButton : Button
    private lateinit var saveButton : Button
    private lateinit var backButton : ImageButton

    private var base64Profile = ""
    private lateinit var userProfile: profile

    private val REQUEST_IMAGE_CAPTURE = 1
    private val FILE_SELECT_CODE = 2
    private val REQUEST_CAMERA_PERMISSION = 3

    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val sessionManager = SessionManager(this)
        val user = sessionManager.getUserDetails()

        profilePic = findViewById(R.id.profile_image)
        editPictureButton = findViewById(R.id.change_image_icon)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.delete_picture)
        backButton = findViewById(R.id.back_button)


        loadProfile()


        editPictureButton.setOnClickListener {
            val bottomSheet = UserImageBottomSheet()
            bottomSheet.setOnImageOptionClickListener(object : UserImageBottomSheet.OnImageOptionClickListener {
                override fun onViewImage() {
                    Toast.makeText(this@EditProfileActivity,"view image",Toast.LENGTH_SHORT).show()
                    PhotoPreviewActivity.imageBitmap = profilePic.drawable.toBitmap()
                    val intent = Intent(this@EditProfileActivity,PhotoPreviewActivity::class.java)
                    startActivity(intent)
                }

                override fun onChangeImageFromCamera() {
                    Toast.makeText(this@EditProfileActivity,"Select image from camera",Toast.LENGTH_SHORT).show()
                    dispatchTakePictureIntent()
                }

                override fun onChangeImageFromStorage() {
                    Toast.makeText(this@EditProfileActivity,"Select image from files",Toast.LENGTH_SHORT).show()
                    selectFile()
                }
            })
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        deleteButton.setOnClickListener{
            base64Profile = ""
            profilePic.setImageResource(R.drawable.default_profile_picture)
        }

        saveButton.setOnClickListener{
            updateProfilePicture(userProfile)
        }

        backButton.setOnClickListener {
            finish()
        }



    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    private fun decodeBase64(base64Str: String): Bitmap? {
        return try {
            if (base64Str.isNotEmpty()) {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            Log.e(ContentValues.TAG, "Error decoding Base64 string", e)
            null
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, FILE_SELECT_CODE)
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If the camera permission is not granted, request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            // If permission is already granted, proceed with taking the picture
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        ex.printStackTrace()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "${packageName}.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Load the image from the saved file path
            currentPhotoPath?.let { path ->
                val imageBitmap = BitmapFactory.decodeFile(path)
                profilePic.setImageBitmap(imageBitmap)
                base64Profile = encodeBitmapToBase64(imageBitmap)

            }
        } else if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val selectedBitmap = BitmapFactory.decodeStream(inputStream)
                    profilePic.setImageBitmap(selectedBitmap)
                    base64Profile= encodeBitmapToBase64(selectedBitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }


        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        // Convert the bitmap to a byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 15, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Encode the byte array to a Base64 string
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }




    private fun loadProfile() {
        val sessionManager = SessionManager(this)
        val user = sessionManager.getUserDetails()
        val id = user[SessionManager.KEY_USER_ID]
        val apiService = RetroFitClient.apiService

        if (id != null) {
            val call = apiService.getProfileById(id)
            call.enqueue(object : Callback<profile> {
                override fun onResponse(call: Call<profile>, response: Response<profile>) {
                    if (response.isSuccessful) {
                        userProfile = response.body()!!
                        // Update UI with the loaded profile
                        if(userProfile.picture!="") {
                            base64Profile = userProfile.picture
                            profilePic.setImageBitmap(decodeBase64(base64Profile))
                        }
                    }
                    else{
                        finish()
                    }
                }
                override fun onFailure(call: Call<profile>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "Couldn't find the profile\nTry again", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        }
    }

    private fun updateProfilePicture(profile: profile) {
        val sessionManager = SessionManager(this)
        val newPictureBase64 = encodeBitmapToBase64(profilePic.drawable.toBitmap())
        profile.picture = base64Profile

        val apiService = RetroFitClient.apiService
        val updateCall = apiService.updateProfile(profile.id, profile)
        updateCall.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity, "Picture updated", Toast.LENGTH_SHORT).show()
                    sessionManager.changeUserPicture(newPictureBase64)
                } else {
                    Toast.makeText(this@EditProfileActivity, "Couldn't update the picture\nTry again", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Network error\nTry again", Toast.LENGTH_SHORT).show()
            }
        })
    }

}