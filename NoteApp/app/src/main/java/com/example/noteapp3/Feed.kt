package com.example.noteapp3

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class Feed : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profileId: String
    private lateinit var userName: String
    private lateinit var profilePicture: String
    private lateinit var userPicture: CircleImageView
    private var backPressedTime: Long = 0
    private lateinit var sessionManager: SessionManager
    private lateinit var navView: BottomNavigationView
    private lateinit var liveStreaming: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {



        Log.d(TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)


        sessionManager = SessionManager(this)
        val user = sessionManager.getUserDetails()
        profileId = user[SessionManager.KEY_USER_ID]?: ""
        userName = user[SessionManager.KEY_USER_NAME]?: ""
        profilePicture = user[SessionManager.KEY_USER_PICTURE]?: ""

        getFCMToken(profileId)

        navView = findViewById(R.id.bottom_nav_bar)

        navView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.home -> {
                    selectedFragment = HomeFragment()
                    val bundle = Bundle()
                    bundle.putString("name", userName)
                    bundle.putString("profile_id", profileId)
                    selectedFragment.arguments = bundle
                }

                R.id.map -> {
                    selectedFragment = MapFragment()
                    val bundle = Bundle()
                    bundle.putString("profile_id", profileId)
                    selectedFragment.arguments = bundle
                }

                R.id.polls -> {
                    selectedFragment = PollsFragment()
                    val bundle = Bundle()
                    bundle.putString("profile_id", profileId)
                    selectedFragment.arguments = bundle
                }
                R.id.ml ->{

                    selectedFragment = MlFragment()
                    val bundle = Bundle()
                    selectedFragment.arguments = bundle
                }

            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }

        // Set default selection
        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.home
        }



        //drawer
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val navName: TextView = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name)
        navName.text = userName

        userPicture = navigationView.getHeaderView(0).findViewById(R.id.nav_header_image)
        if(profilePicture!="") {
            val imageBytes = Base64.decode(profilePicture, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            userPicture.setImageBitmap(decodedImage)
        }
        else{
            userPicture.setImageResource(R.drawable.default_profile_picture)
        }

        userPicture.setOnClickListener{
            val intent = Intent(this,EditProfileActivity::class.java)
            startActivity(intent)
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        liveStreaming = findViewById(R.id.live)
        liveStreaming.setOnClickListener {
            val intent = Intent(this,LiveStreaming::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()

        //update name and picture
        userName = sessionManager.getUserDetails()[SessionManager.KEY_USER_NAME]?: ""
        profilePicture = sessionManager.getUserDetails()[SessionManager.KEY_USER_PICTURE]?: ""

        if(profilePicture==""){
            userPicture.setImageResource(R.drawable.default_profile_picture)
        }
        else{
            val imageBytes = Base64.decode(profilePicture, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            userPicture.setImageBitmap(decodedImage)
        }

    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else if (backPressedTime + 2000 > System.currentTimeMillis()) {
            //super.onBackPressed()
            moveTaskToBack(true) //keep the app in the background
        }
        backPressedTime = System.currentTimeMillis()

    }





    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var selectedFragment: Fragment? = null
        when (item.itemId) {

            R.id.nav_notifcations -> {
                selectedFragment = Notifications()
                val bundle = Bundle()
                selectedFragment.arguments = bundle
            }
            R.id.nav_settings -> {
                selectedFragment = SettingsFragment()
                val bundle = Bundle()
                selectedFragment.arguments = bundle
            }

            R.id.nav_logout -> {

                // Navigate to the login screen
                val intent = Intent(this, LoginPage::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                val user = sessionManager.getUserDetails()
                val id = user[SessionManager.KEY_USER_ID]?:""
                removeTokenFromFirestore(id)
                sessionManager.logoutUser()
            }

        }
        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            // Clear the selected state of all navigation items

            navView.menu.findItem(R.id.home).isChecked = false
            navView.menu.findItem(R.id.map).isChecked = false
            navView.menu.findItem(R.id.polls).isChecked = false



        }

        drawerLayout.closeDrawer(GravityCompat.START)


        return true
    }

    private fun getFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Token: $token")

            // Save token to sessionManager
            sessionManager.saveToken(token)

            // Save token to Firestore
            val dbFireStore = Firebase.firestore
            val userRef = dbFireStore.collection("users").document(userId)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Update existing document
                    userRef.update("token", token)
                        .addOnSuccessListener {
                            Log.d(TAG, "Token updated for user: $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating token for user: $userId", e)
                        }
                } else {
                    // Create new document
                    val userToken = mapOf("token" to token)
                    userRef.set(userToken)
                        .addOnSuccessListener {
                            Log.d(TAG, "Token saved for new user: $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error saving token for new user: $userId", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error checking user document existence for $userId", e)
            }
        }
    }

    private fun removeTokenFromFirestore(userId: String) {

        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)
        userRef.update("token","" )
    }
    companion object {
        private const val TAG = "Feed"
    }
}








