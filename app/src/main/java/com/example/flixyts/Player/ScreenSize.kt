package com.example.flixyts.Player

import android.util.Log

class ScreenSize(private val root_width:Int, private val root_height:Int, private val video_width:Int, private val video_height:Int){
    fun fitScreen(ratio:Float?): Pair<Int,Int>
    {
        Log.d("ScreenSize","root_width : $root_width && root_height : $root_height ::: ${root_width*1f/root_height}")
        Log.d("ScreenSize","root_width : $video_width && root_height : $video_height ::: ${video_width*1f/video_height}")
        val rootRatio= root_width*1f/root_height
        var aspectRatio = video_width*1f/video_height
        if(ratio!=null)
            aspectRatio = ratio

        var requiredHeight = 0
        var requiredWidth = 0
        if(aspectRatio > rootRatio ) {
            requiredWidth = root_width
            requiredHeight = (root_width/aspectRatio).toInt()

        }
        else{
            requiredHeight = root_height
            requiredWidth = (root_height*aspectRatio).toInt()
        }
        return Pair(requiredWidth,requiredHeight)
    }
}