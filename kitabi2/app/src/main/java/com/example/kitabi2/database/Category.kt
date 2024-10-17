package com.example.kitabi2.database

data class Category(
    var id:Int = -1,
    var Category:String="",
    var books: List<Book> = mutableListOf()

)
