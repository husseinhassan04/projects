package com.example.kitabi2.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "cart_books")
data class CartBook(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val price: Double,
    var description:String,
    var url:String,
    var quantity:Int
)