package com.example.flixyts.Models

import android.os.Parcel
import java.io.Serializable

class MovieDownloadFile(private var name: String?, private var path: String?, private var year: String?, private var resolution:String?, private var clip:String?, private var async_started:Boolean = false, private var async_ended:Boolean = false) : Serializable
{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    fun getName(): String? = name
    fun getPath(): String? = path
    fun getYear(): String? = year
    fun getResolution():String? = resolution
    fun getClip():String? = clip
    fun isAsyncStarted():Boolean = async_started
    fun setAsyncStarted(started:Boolean){
        async_started = started
    }

    fun isAsyncEnded():Boolean = async_ended
    fun setAsyncEnded(ended:Boolean){
        async_ended = ended
    }

    fun setMovieClip(imagePath:String) {
        clip = imagePath
    }
}