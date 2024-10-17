package com.example.kitabi2

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.kitabi2.database.Book
import com.example.kitabi2.roomdb.AppDatabase
import com.example.kitabi2.roomdb.BookDao
import com.example.kitabi2.roomdb.CartBook
import com.example.kitabi2.roomdb.WishlistBook
import com.example.kitabi2.ui.cart.CartBottomSheet
import kotlinx.coroutines.launch

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var backBtn: Button
    private lateinit var wishListBtn: ImageButton

    private lateinit var coverPicture: ImageView
    private lateinit var title: TextView
    private lateinit var author: TextView
    private lateinit var description: TextView
    private lateinit var price: TextView

    private lateinit var addToCartBtn: Button

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        db = AppDatabase.getDatabase(this)
        val bookDao = db.bookDao()

        backBtn = findViewById(R.id.back_button)
        backBtn.setOnClickListener {
            finish()
        }

        wishListBtn = findViewById(R.id.wishlist_button)

        // Views setting
        coverPicture = findViewById(R.id.cover)
        title = findViewById(R.id.title)
        author = findViewById(R.id.author)
        price = findViewById(R.id.price)
        description = findViewById(R.id.description)

        // Syncing book with views
        val mainBook = Book(
            intent.getStringExtra("id") ?: "",
            intent.getStringExtra("title") ?: "",
            intent.getStringExtra("author") ?: "",
            intent.getIntExtra("category", -1),
            intent.getStringExtra("description") ?: "",
            intent.getDoubleExtra("price", 0.0),
            intent.getStringExtra("url") ?: ""
        )

        title.text = mainBook.title
        author.text = mainBook.author
        price.text = "Price:$" + mainBook.price.toString()
        description.text = mainBook.Description

        // Setting image using Glide
        Glide.with(this).load(mainBook.url).into(coverPicture)

        // Check if book is in the wishlist and set the initial icon
        lifecycleScope.launch {
            val isInWishlist = isInWishList(bookDao, mainBook.id)
            if (isInWishlist) {
                wishListBtn.setImageResource(R.drawable.icon_wishlist_selected)
            } else {
                wishListBtn.setImageResource(R.drawable.icon_wishlist)
            }
        }

        val sessionManager:SessionManager = SessionManager(this)

        // Handle wishlist button click
        wishListBtn.setOnClickListener {

            if(sessionManager.isLoggedIn()) {

                val wishListBook = WishlistBook(
                    mainBook.id,
                    mainBook.title,
                    mainBook.author,
                    mainBook.price,
                    mainBook.Description,
                    mainBook.url
                )

                lifecycleScope.launch {
                    val result = isInWishList(bookDao, wishListBook.id)

                    if (result) {
                        // Book exists, remove it from the wishlist
                        bookDao.deleteWishlistBook(wishListBook)
                        wishListBtn.setImageResource(R.drawable.icon_wishlist)
                    } else {
                        // Book doesn't exist, add it to the wishlist
                        bookDao.insertWishlistBook(wishListBook)
                        wishListBtn.setImageResource(R.drawable.icon_wishlist_selected)
                    }
                }
            }
            else{
                Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show()
            }
        }

        // Add to cart functionality
        addToCartBtn = findViewById(R.id.addToCartButton)

        addToCartBtn.setOnClickListener {

            if (sessionManager.isLoggedIn()) {

                // Open the Cart Bottom Sheet
                CartBottomSheet(mainBook.title) { quantity ->
                    val cartBook = CartBook(
                        mainBook.id,
                        mainBook.title,
                        mainBook.author,
                        mainBook.price,
                        mainBook.Description,
                        mainBook.url,
                        quantity
                    )
                    lifecycleScope.launch {
                        bookDao.insertCartBook(cartBook)

                    }
                    Toast.makeText(this, "Added $quantity items to cart", Toast.LENGTH_SHORT).show()

                }.show(supportFragmentManager, "CartBottomSheet")
            } else {
                Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Check if the book is already in the wishlist
    private suspend fun isInWishList(bookDao: BookDao, id: String): Boolean {
        return bookDao.checkIfBookExistsInWishlist(id)
    }
}
