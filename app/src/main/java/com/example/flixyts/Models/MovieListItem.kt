package com.example.flixyts.Models

import java.io.Serializable

data class MovieListItem(
    var title:String?,
    var imdb_code:String?,
    var poster_link:String?
) : Serializable