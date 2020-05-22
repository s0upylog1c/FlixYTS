package com.example.flixyts.Models

import com.google.gson.annotations.SerializedName

data class Post(
    val status:String,
    val status_message:String,
    val data: Data,
    @SerializedName("@meta")val meta: Meta
    )
