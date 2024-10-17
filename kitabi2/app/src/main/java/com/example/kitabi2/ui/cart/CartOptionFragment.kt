package com.example.kitabi2.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kitabi2.R
import com.example.kitabi2.adapters.CartAdapter
import com.example.kitabi2.roomdb.AppDatabase
import com.example.kitabi2.roomdb.CartBook
import kotlinx.coroutines.launch

class CartOptionFragment : Fragment() {

    private lateinit var adapter: CartAdapter
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var proceedToCheckOutBtn:Button
    private lateinit var books: MutableList<CartBook>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.cart_option_cart_fragment, container, false)

        db = AppDatabase.getDatabase(requireContext())
        val bookDao = db.bookDao()

        proceedToCheckOutBtn = view.findViewById(R.id.check_out)

        // Initialize RecyclerView and Adapter with an empty list
        recyclerView = view.findViewById(R.id.cart_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CartAdapter(requireContext(), mutableListOf(),bookDao) // Initially empty list
        recyclerView.adapter = adapter

        // Load the data asynchronously and update the adapter
        lifecycleScope.launch {
            books = bookDao.getAllCartBooks()
            adapter.updateBooks(books) // Update with the fetched data
        }

        proceedToCheckOutBtn.setOnClickListener {
            val intent = Intent(requireContext(),CheckOutActivity::class.java)
            intent.putExtra("totalPrice",getCartTotalPrice())
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Update the list when the fragment is resumed
        lifecycleScope.launch {
            books = db.bookDao().getAllCartBooks()
            adapter.updateBooks(books)
        }
    }


    private fun getCartTotalPrice(): String {
        var totalPrice = 0.0
        for (book in books) {
            totalPrice += (book.quantity * book.price)
        }
        return totalPrice.toString()
    }
}
