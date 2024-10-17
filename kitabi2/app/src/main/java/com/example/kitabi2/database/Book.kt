package com.example.kitabi2.database

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val category:Int=-1,
    val Description: String = "",
    val price: Double = 0.0,
    val url: String = ""
)
