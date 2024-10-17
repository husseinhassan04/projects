package com.example.kitabi2.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Or use Picasso if you prefer
import com.example.kitabi2.BookDetailsActivity
import com.example.kitabi2.R
import com.example.kitabi2.database.Book

class SearchResultAdapter(
    private val context: Context,
    private val bookList: List<Book>
) : RecyclerView.Adapter<SearchResultAdapter.BookViewHolder>() {

    // ViewHolder class to hold the views
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImage: ImageView = itemView.findViewById(R.id.cover_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val author: TextView = itemView.findViewById(R.id.author)
        val container:LinearLayout = itemView.findViewById(R.id.container)

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Inflate the custom layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result, parent, false) // Inflate your XML layout
        return BookViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        // Get element from your dataset at this position
        val book = bookList[position]

        // Set item views based on your views and data model
        holder.title.text = book.title
        holder.description.text = book.Description
        holder.author.text = book.author

        holder.container.setOnClickListener {
            val intent = Intent(context, BookDetailsActivity::class.java)

            intent.putExtra("title",book.title)
            intent.putExtra("author",book.author)
            intent.putExtra("description",book.Description)
            intent.putExtra("url",book.url)
            intent.putExtra("id",book.id)
            intent.putExtra("price",book.price)
            context.startActivity(intent)
        }

        // Use Glide or Picasso to load the image into the ImageView
        Glide.with(context)
            .load(book.url)
            .into(holder.coverImage)
    }

    // Return the size of your dataset
    override fun getItemCount() = bookList.size
}
