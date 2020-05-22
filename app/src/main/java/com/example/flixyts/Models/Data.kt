package com.example.flixyts.Models

data class Data(
    val movie_count:Int,
    val limit:Int,
    val page_number:Int,
    val movies:List<Movie>
)