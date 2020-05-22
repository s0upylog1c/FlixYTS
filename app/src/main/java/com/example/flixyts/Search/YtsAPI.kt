package com.example.flixyts.Search

import com.example.flixyts.Models.Post
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YtsAPI{

    @GET("list_movies.json")
    fun getQuery(@Query("query_term")query_term:String,@Query("genre")genre:String,@Query("minimum_rating")minimum_rating:Int): Call<Post>
}