package com.example.kitabi2.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kitabi2.BookDetailsActivity
import com.example.kitabi2.R
import com.example.kitabi2.database.Book

class BooksAdapter(private val books: List<Book>,private val context: Context) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookImage: ImageView = view.findViewById(R.id.cover)
        val bookTitle: TextView = view.findViewById(R.id.title)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        // Load book image and title
        holder.bookTitle.text = book.title
        // Use an image loading library like Glide or Picasso to load book images
        if(book.url.isNotBlank()) {
            Glide.with(holder.bookImage.context).load(book.url).into(holder.bookImage)
        }

        holder.bookImage.setOnClickListener {
            val intent = Intent(context,BookDetailsActivity::class.java)
            intent.putExtra("title",book.title)
            intent.putExtra("author",book.author)
            intent.putExtra("description",book.Description)
            intent.putExtra("url",book.url)
            intent.putExtra("id",book.id)
            intent.putExtra("category",book.category)
            intent.putExtra("price",book.price)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int = books.size
}
