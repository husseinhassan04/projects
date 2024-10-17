package com.example.kitabi2

import android.app.Application
import com.example.kitabi2.database.Book
import com.example.kitabi2.database.Category
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class KitabiApplication : Application() {

    private lateinit var db: FirebaseFirestore
    var booksList: MutableList<Book> = mutableListOf() // List to hold books
    var categoriesList: List<Category> = listOf() // List to hold categories

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        // Fetch books and categories when the application starts
        fetchBooksFromFirestore { books ->
            booksList = books.toMutableList()
            assignBooksToCategories() // Assign books to categories after fetching
        }

        fetchCategoriesFromFirestore { categories ->
            categoriesList = categories
            assignBooksToCategories() // You may also want to call this here if categories are fetched first
        }
    }

    fun fetchBooksFromFirestore(onFetchComplete: (List<Book>) -> Unit) {
        db.collection("books")
            .get()
            .addOnSuccessListener { documents ->
                val books = mutableListOf<Book>()
                for (document in documents) {
                    val book = document.toObject(Book::class.java)
                    books.add(book)
                }
                println("Fetched books: $books") // Log fetched books
                onFetchComplete(books)
            }
            .addOnFailureListener { e ->
                println("Error fetching books: $e")
                onFetchComplete(emptyList())
            }
    }

    fun fetchCategoriesFromFirestore(onFetchComplete: (List<Category>) -> Unit) {
        db.collection("Categories")
            .get()
            .addOnSuccessListener { documents ->
                val categories = mutableListOf<Category>()
                for (document in documents) {
                    val category = document.toObject(Category::class.java)
                    categories.add(category)
                }
                println("Fetched categories: $categories") // Log fetched categories
                onFetchComplete(categories)
            }
            .addOnFailureListener { e ->
                println("Error fetching categories: $e")
                onFetchComplete(emptyList())
            }
    }


        private fun assignBooksToCategories() {
        // Create a map of categoryId to list of books
        val categoryBooksMap = booksList.groupBy { it.category }

        // Assign the books to their respective categories
        categoriesList.forEach { category ->
            category.books =
                (categoryBooksMap[category.id] ?: emptyList()).toMutableList() // Use empty list if no books found
        }
    }
}
