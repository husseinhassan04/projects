package com.example.noteapp3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpPage : AppCompatActivity() {
    private lateinit var newProfile: profile
    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var camera: ImageButton
    private lateinit var accessFilesBtn: ImageButton
    private lateinit var profilePreview: ImageView
    private lateinit var deletePhoto: LinearLayout
    private lateinit var confirm: ImageButton
    private lateinit var datePicker: DatePicker

    private val REQUEST_IMAGE_CAPTURE = 1
    private val FILE_SELECT_CODE = 2
    private val REQUEST_CAMERA_PERMISSION = 3

    private var currentPhotoPath: String? = null
    private var profileImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page)

        name = findViewById(R.id.name)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmpassword)
        camera = findViewById(R.id.button_take_photo)
        accessFilesBtn = findViewById(R.id.accessFilesBtn)
        confirm = findViewById(R.id.button_signup)
        datePicker = findViewById(R.id.date_picker)
        profilePreview = findViewById(R.id.preview_picture)
        deletePhoto = findViewById(R.id.delete_photo)

        newProfile = profile()

        camera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        accessFilesBtn.setOnClickListener{
            selectFile()
        }

        confirm.setOnClickListener {
            if (password.text.toString() == confirmPassword.text.toString()) {
                newProfile.name = name.text.toString()
                newProfile.user = username.text.toString()
                newProfile.pass = md5(password.text.toString())
                newProfile.picture = ""

                val day = datePicker.dayOfMonth
                val month = datePicker.month + 1 // Month is 0-based in DatePicker
                val year = datePicker.year

                newProfile.dateOfBirth = String.format("%02d-%02d-%04d", year,month,day)
                // Save profile with image here
                if (profileImageBitmap != null) {
                    newProfile.picture = encodeBitmapToBase64(profileImageBitmap!!)
                }
                if(newProfile.name.isEmpty()){
                    name.error ="field can't be empty"
                }
                else if(newProfile.user.isEmpty()){
                    username.error ="field can't be empty"
                }
                else if(password.text.toString().length<8){
                    password.error = "password should be 8 characters or more"
                }
                else {
                    createProfile(newProfile)


                }
            }
            else{
                confirmPassword.error = "should match with the previous password"
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this,LoginPage::class.java)
        startActivity(intent)
    }

    private fun md5(pass: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(pass.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
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

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, proceed with taking the picture
                    dispatchTakePictureIntent()
                } else {
                    // Permission was denied, inform the user with a message or handle it appropriately
                    Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Load the image from the saved file path
            currentPhotoPath?.let { path ->
                val imageBitmap = BitmapFactory.decodeFile(path)
                profileImageBitmap = imageBitmap


                //enable the delete functionality
                profilePreview.visibility = ImageView.VISIBLE
                deletePhoto.visibility = LinearLayout.VISIBLE

                profilePreview.setImageBitmap(profileImageBitmap)
                profilePreview.setOnClickListener {
                    PhotoPreviewActivity.imageBitmap = profileImageBitmap
                    val intent = Intent(this@SignUpPage, PhotoPreviewActivity::class.java)
                    startActivity(intent)
                }

                deletePhoto.setOnClickListener {
                    val builder = AlertDialog.Builder(this@SignUpPage)
                    builder.setMessage("Are you sure you want to delete this post?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, id ->

                            profilePreview.visibility = ImageView.GONE
                            deletePhoto.visibility = LinearLayout.GONE
                            profileImageBitmap = null

                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                }
            }
        } else if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val selectedBitmap = BitmapFactory.decodeStream(inputStream)
                    profileImageBitmap= selectedBitmap
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            //enable the delete functionality
            profilePreview.visibility = ImageView.VISIBLE
            deletePhoto.visibility = LinearLayout.VISIBLE

            profilePreview.setImageBitmap(profileImageBitmap)
            profilePreview.setOnClickListener {
                PhotoPreviewActivity.imageBitmap = profileImageBitmap
                val intent = Intent(this@SignUpPage, PhotoPreviewActivity::class.java)
                startActivity(intent)
            }

            deletePhoto.setOnClickListener {
                val builder = AlertDialog.Builder(this@SignUpPage)
                builder.setMessage("Are you sure you want to delete this post?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->

                        profilePreview.visibility = ImageView.GONE
                        deletePhoto.visibility = LinearLayout.GONE
                        profileImageBitmap = null

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
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

    private fun createProfile(profile: profile) {
        val call: Call<profile> = RetroFitClient.apiService.createProfile(profile)
        call.enqueue(object : Callback<profile> {
            override fun onResponse(call: Call<profile>, response: Response<profile>) {
                if (response.isSuccessful) {
                    val createdProfile = response.body()
                    createdProfile?.let {
                        // Profile created successfully
                        Toast.makeText(this@SignUpPage, "Profile created successfully", Toast.LENGTH_SHORT).show()
                        // Proceed to Feed or other activity
                        val intent = Intent(this@SignUpPage, Feed::class.java)
                        val sessionManager = SessionManager(this@SignUpPage)

                        sessionManager.createLoginSession(newProfile.id,newProfile.name,newProfile.picture)
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(this@SignUpPage, "Failed to retrieve profile data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUpPage, "Failed to create profile: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<profile>, t: Throwable) {
                Toast.makeText(this@SignUpPage, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }


}
