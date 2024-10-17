package com.example.kitabi2.ui.cart


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kitabi2.R
import com.example.kitabi2.adapters.CartAdapter
import com.example.kitabi2.adapters.WishlistAdapter
import com.example.kitabi2.roomdb.AppDatabase
import com.example.kitabi2.roomdb.CartBook
import com.example.kitabi2.roomdb.WishlistBook
import kotlinx.coroutines.launch

class WishlistOptionFragment : Fragment() {

    private lateinit var adapter: WishlistAdapter
    private lateinit var db:AppDatabase
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.wishlist_option_cart_fragment, container, false)


        db = AppDatabase.getDatabase(requireContext())
        val bookDao= db.bookDao()

        // Initialize RecyclerView and Adapter with an empty list
        recyclerView = view.findViewById(R.id.wishlist_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = WishlistAdapter(requireContext(), mutableListOf()) // Initially empty list
        recyclerView.adapter = adapter

        lifecycleScope.launch {


            val books = bookDao.getAllWishlistBooks()
            adapter.updateBooks(books) // Update with the fetched data


        }

        return view
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val books = db.bookDao().getAllWishlistBooks()
            adapter.updateBooks(books)
        }
    }
}

