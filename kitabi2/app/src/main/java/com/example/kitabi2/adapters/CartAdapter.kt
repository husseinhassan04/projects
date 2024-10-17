package com.example.kitabi2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Or use Picasso if you prefer
import com.example.kitabi2.R
import com.example.kitabi2.database.Book
import com.example.kitabi2.roomdb.BookDao
import com.example.kitabi2.roomdb.CartBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartAdapter(
    private val context: Context,
    private val bookList: MutableList<CartBook>,
    private val  bookDao: BookDao
) : RecyclerView.Adapter<CartAdapter.BookViewHolder>() {

    // ViewHolder class to hold the views
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImage: ImageView = itemView.findViewById(R.id.cover_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val price: TextView = itemView.findViewById(R.id.price)
        val author: TextView = itemView.findViewById(R.id.author)
        val increaseBtn: ImageButton = itemView.findViewById(R.id.btn_increase)
        val decreaseBtn: ImageButton = itemView.findViewById(R.id.btn_decrease)
        val removeFromCart: ImageButton = itemView.findViewById(R.id.remove_from_cart)


    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Inflate the custom layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_content, parent, false) // Inflate your XML layout
        return BookViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        // Get element from your dataset at this position
        val book = bookList[position]

        // Set item views based on your views and data model
        holder.title.text = book.title
        holder.author.text = book.author
        holder.quantity.text = "Quantity: "+book.quantity

        //take 2 digits after the dot of the price
        val price = String.format("%.2f", book.price*book.quantity)
        holder.price.text = "Price: $"+price

        holder.decreaseBtn.setOnClickListener {
            if(book.quantity>1) {
                book.quantity -= 1

                GlobalScope.launch {
                    bookDao.modifyCartBook(book)

                    withContext(Dispatchers.Main) {
                        // Notify adapter about data changes
                        notifyDataSetChanged()
                    }                }
            }
        }

        holder.increaseBtn.setOnClickListener {
            book.quantity+=1

            GlobalScope.launch {
                bookDao.modifyCartBook(book)

                withContext(Dispatchers.Main) {
                    // Notify adapter about data changes
                    notifyDataSetChanged()
                }
            }
        }
        holder.removeFromCart.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val bookToRemove = bookList[position]

                GlobalScope.launch {
                    // Delete from the database
                    bookDao.deleteCartBook(bookToRemove)

                    // Switch to the main thread to update the UI
                    withContext(Dispatchers.Main) {
                        // Remove the item from the bookList
                        bookList.removeAt(position)

                        // Notify the adapter that the item has been removed
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, bookList.size)  // Optional, to fix position updates
                    }
                }
            }
        }


        // Use Glide or Picasso to load the image into the ImageView
        Glide.with(context)
            .load(book.url)
            .into(holder.coverImage)


    }

    // Return the size of your dataset
    override fun getItemCount() = bookList.size
    fun updateBooks(books: List<CartBook>) {
        bookList.clear()
        bookList.addAll(books)
        notifyDataSetChanged()
    }
}
