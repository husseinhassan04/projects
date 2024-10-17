package com.example.kitabi2.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.kitabi2.ui.cart.CartOptionFragment
import com.example.kitabi2.ui.cart.WishlistOptionFragment

class CartWishlistPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2 // Cart and Wishlist

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CartOptionFragment()     // Cart Fragment
            1 -> WishlistOptionFragment() // Wishlist Fragment
            else -> CartOptionFragment()  // Default to Cart
        }
    }
}
