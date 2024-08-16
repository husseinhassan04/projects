package com.example.noteapp3

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.RetroFitClient.apiService
import com.example.noteapp3.models.profile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPage : AppCompatActivity() {

    private val TAG = "ProfileListActivity"
    private val PERMISSIONS_REQUEST_CODE = 123

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private var profiles: List<profile>? = emptyList()
    private val emailProfile = profile()
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)



        Log.d(TAG, "onCreate() called")
        fetchProfiles()

        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)

        if (!checkPermissions()) {
            requestPermissions()
        }

        val loginButton = findViewById<Button>(R.id.login_btn)
        loginButton.setOnClickListener {
            fetchProfiles()
            login()
        }

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInButton:Button = findViewById(R.id.gmail)


        // Load the google icon to the button
        val originalDrawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.google_icon)

        originalDrawable?.let {
            // Convert Drawable to Bitmap
            val bitmap = (it as BitmapDrawable).bitmap

            // Resize the Bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, false) // Set the width and height as needed

            // Convert Bitmap back to Drawable
            val resizedDrawable = BitmapDrawable(resources, resizedBitmap)

            // Set the resized drawable to the button
            googleSignInButton.setCompoundDrawablesWithIntrinsicBounds(resizedDrawable, null, null, null)
        }
        googleSignInButton.setOnClickListener {
            gmailSignIn()
        }



        signUpButton = findViewById(R.id.signup_btn)
        signUpButton.setOnClickListener {

            val intent = Intent(this, SignUpPage::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finishAffinity()
    }

    private fun login() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val passStr = md5(password)

        profiles?.let { profileList ->
            // Iterate over each profile
            for (profile in profileList) {
                if (username == profile.user && passStr == profile.pass) {
                    sessionManager = SessionManager(this)
                    sessionManager.createLoginSession(profile.id,profile.name,profile.picture)
                    val intent = Intent(this, Feed::class.java)
                    startActivity(intent)
                    finish()
                    return
                }
            }
        }
        Toast.makeText(this, "Wrong credentials", Toast.LENGTH_SHORT).show()
        usernameEditText.text.clear()
        passwordEditText.text.clear()
    }

    private fun md5(pass: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(pass.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun fetchProfiles() {
        val apiService = RetroFitClient.apiService
        val call = apiService.getAllProfiles()

        call.enqueue(object : Callback<List<profile>> {
            override fun onResponse(call: Call<List<profile>>, response: Response<List<profile>>) {
                if (response.isSuccessful) {
                    profiles = response.body()
                    profiles?.let { profileList ->
                        for (profile in profileList) {
                            Log.d(TAG, "Profile: ${profile.user}, Password: ${profile.pass}")
                        }
                    } ?: run {
                        Log.e(TAG, "Empty response body")
                    }
                } else {
                    Log.e(TAG, "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<profile>>, t: Throwable) {
                Log.e(TAG, "Error fetching profiles", t)
            }
        })
    }

    private fun gmailSignIn() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                Log.d(TAG, "firebaseAuthWithGoogle: " + account.id)
                Log.d(TAG, "GoogleSignInAccount details:")
                Log.d(TAG, "Id: ${account.id}")
                Log.d(TAG, "DisplayName: ${account.displayName}")
                Log.d(TAG, "Email: ${account.email}")
                Log.d(TAG, "PhotoUrl: ${account.photoUrl}")

                firebaseAuthWithGoogle(account.idToken!!, account)
            } else {
                Log.w(TAG, "GoogleSignInAccount is null")
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed with ApiException: ${e.statusCode}", e)
        } catch (e: Exception) {
            Log.w(TAG, "Google sign in failed with unexpected exception", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    val password = generateRandomPassword()

                    for(profile in profiles!!) {
                        if (profile.user == account.email) {
                            sessionManager = SessionManager(this)
                            sessionManager.createLoginSession(profile.id, profile.name, profile.picture)
                            val intent = Intent(this, Feed::class.java)
                            startActivity(intent)
                            finish()
                            return@addOnCompleteListener
                        }
                    }


                    emailProfile.name = account.displayName ?: ""
                    emailProfile.user = account.email ?: ""
                    emailProfile.pass = password

                    createProfile(emailProfile)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun createProfile(profile: profile) {
        val call: Call<profile> = apiService.createProfile(profile)
        call.enqueue(object : Callback<profile> {
            override fun onResponse(call: Call<profile>, response: Response<profile>) {
                if (response.isSuccessful) {
                    val createdProfile = response.body()
                    Log.d(TAG, "Profile created: $createdProfile")
                    val intent = Intent(this@LoginPage, Feed::class.java)
                    intent.putExtra("profile_id", profile.id)
                    intent.putExtra("name", profile.name)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(TAG, "Failed to create profile: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<profile>, t: Throwable) {
                Log.e(TAG, "Error: ${t.message}")
                Toast.makeText(this@LoginPage, "Problem occurred\nCheck your connection", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateRandomPassword(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@!$#%^&*()"
        return (1..28)
            .map { allowedChars.random() }
            .joinToString("")
    }


    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.POST_NOTIFICATIONS
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false // Return false if any permission is not granted
            }
        }
        return true // Return true if all permissions are granted
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.POST_NOTIFICATIONS
        )

        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val deniedPermissions = permissions
                .filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
                .toList()

            if (deniedPermissions.isEmpty()) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions not granted: ${deniedPermissions.joinToString()}", Toast.LENGTH_SHORT).show()

            }
        }
    }
}
