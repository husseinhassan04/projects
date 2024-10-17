package com.example.kitabi2.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.kitabi2.LoginActivity
import com.example.kitabi2.R
import com.example.kitabi2.SessionManager
import com.example.kitabi2.adapters.CartWishlistPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CartFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var loggedInLayout: View
    private lateinit var loggedOutLayout: View
    private lateinit var rootView: View // Store the root view of the fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sessionManager = SessionManager(requireContext())
        rootView = inflater.inflate(R.layout.fragment_cart, container, false)

        loggedInLayout = rootView.findViewById(R.id.logged_in_layout)
        loggedOutLayout = rootView.findViewById(R.id.logged_out_layout)

        if (sessionManager.isLoggedIn()) {
            showLoggedInLayout(rootView)
        } else {
            showLoggedOutLayout()
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        // Recheck login status and update layout only if needed
        if (sessionManager.isLoggedIn()) {
            if (loggedOutLayout.visibility == View.VISIBLE) {
                showLoggedInLayout(rootView)
            }
        } else {
            if (loggedInLayout.visibility == View.VISIBLE) {
                showLoggedOutLayout()
            }
        }
    }


    private fun showLoggedInLayout(view: View) {
        loggedInLayout.visibility = View.VISIBLE
        loggedOutLayout.visibility = View.GONE
        // Load your cart items or perform other operations here
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)

        // Set up ViewPager2 with the adapter
        val pagerAdapter = CartWishlistPagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        // Link TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Cart" else "Wishlist"
        }.attach()
    }

    private fun showLoggedOutLayout() {
        loggedInLayout.visibility = View.GONE
        loggedOutLayout.visibility = View.VISIBLE

        val loginBtn: TextView = loggedOutLayout.findViewById(R.id.login)
        loginBtn.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)

        }
    }
}
