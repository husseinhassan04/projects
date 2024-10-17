package com.example.kitabi2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.kitabi2.databinding.ActivityFeedBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var loginBtn: Button
        private lateinit var profileBtn: CircleImageView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize buttons
        loginBtn = findViewById(R.id.loginBtn)
        profileBtn = findViewById(R.id.profile)

        // Set initial visibility based on login status
        updateButtonVisibility()

        // Set button click listeners
        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // Set up NavController and AppBarConfiguration
        val navController = findNavController(R.id.nav_host_fragment_activity_feed)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_catalog,
            R.id.navigation_search,
            R.id.navigation_cart
        ))

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up BottomNavigationView
        val bottomNavView: BottomNavigationView = binding.navView
        bottomNavView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_feed)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        // Update button visibility whenever the activity resumes
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        if (sessionManager.isLoggedIn()) {
            Glide.with(this).load(sessionManager.getUserProfilePic()).into(profileBtn)
            loginBtn.visibility = View.GONE
            profileBtn.visibility = View.VISIBLE
        } else {
            loginBtn.visibility = View.VISIBLE
            profileBtn.visibility = View.GONE
        }
    }
}
