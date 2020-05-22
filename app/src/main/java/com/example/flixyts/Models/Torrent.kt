package com.example.flixyts.Models

import java.io.Serializable

data class Torrent(
    val url:String,
    val hash:String,
    val quality:String,
    val type:String,
    val seeds:Int,
    val peers:Int,
    val size:String,
    val size_bytes:Long,
    val date_uploaded:String,
    val date_uploaded_unix:Int
) : Serializable