package com.example.kitabi2.roomdb


import androidx.room.*

@Dao
interface BookDao {

    // Wishlist DAO functions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistBook(book: WishlistBook)

    @Delete
    suspend fun deleteWishlistBook(book: WishlistBook)

    @Query("SELECT * FROM wishlist_books")
    suspend fun getAllWishlistBooks(): MutableList<WishlistBook>

    @Query("SELECT COUNT(*) > 0 FROM wishlist_books WHERE id = :bookId")
    suspend fun checkIfBookExistsInWishlist(bookId: String): Boolean


    // Cart DAO functions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartBook(book: CartBook)

    @Delete
    suspend fun deleteCartBook(book: CartBook)

    @Query("SELECT * FROM cart_books")
    suspend fun getAllCartBooks(): MutableList<CartBook>

    @Update
    suspend fun modifyCartBook(book: CartBook)


    @Query("DELETE FROM cart_books")
    suspend fun emptyCart()
}
