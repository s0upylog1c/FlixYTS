package com.example.flixyts.Downloads

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.example.flixyts.Models.MovieDownloadFile
import java.io.File
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LocalSearch(private val rootPath:String,private val context: Context) {
    private var fileList = ArrayList<HashMap<String,String>>()
    private var movieDownloadFileList = ArrayList<MovieDownloadFile>()

    fun searchForFiles(path:String = rootPath, level:Int = 1): ArrayList<HashMap<String,String>>? {

        if(level>3)
            return null
        try {
            val rootFolder = File(path)
            val files = rootFolder.listFiles()
            val childFolder = File("$path/VLC")
            Log.d("RootFolder","root folder : "+rootFolder.absolutePath)
            Log.d("RootFolder","root folder : "+files.size)
            var depthNow = level+1
            for( file in files ){
                if(file.isDirectory) {
                    var tempListOfFiles = searchForFiles(file.absolutePath,depthNow)
                    if(tempListOfFiles!=null)
                    {
                        Log.d("Level",rootPath+" >> "+file.name+" : "+level)
                        fileList.addAll(tempListOfFiles)
                    }
                    else
                        break;
                }
                else if(file.name.endsWith(".mp4") ||file.name.endsWith(".mkv") /*|| file.name.endsWith(".torrent")*/) {
                    var video = HashMap<String,String>()
                    video["file_path"] = file.absolutePath
                    video["file_name"] = file.name

                    addAfterFilter(file.name,file.absolutePath)
//                    movieDownloadFileList.add(MovieDownloadFile(file.name,file.absolutePath))

                    Log.d("Files","File name : "+video["file_name"])
                    Log.d("Files","File path : "+video["file_path"])
                    var retriever = MediaMetadataRetriever()
                    //Log.d("Files","File metadata : "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)+" <> "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    //Task<Metadata> getMetadataTask =
                    fileList.add(video)
                }
            }
        }
        catch (e: NullPointerException){
            Log.d("Files","NullPointerException : "+e.localizedMessage)
            Log.d("Files","Stack trace : ")
            e.printStackTrace()
            return null
        }
        return this.fileList
    }
    fun getFileList(): ArrayList<HashMap<String,String>> {
        return fileList
    }

    fun getMovieDownloadFileList() = movieDownloadFileList

    private fun addAfterFilter(name:String, path:String) {

        val tokenizer: StringTokenizer = StringTokenizer(name,".")
        var fileName:String = ""
        lateinit var token:String
        do {
            token = tokenizer.nextToken()
            if((token.length==4 && token.matches("^[0-9]*$".toRegex())) || (token.length>=6 && (token[0]=='s'||token[0]=='S') && token[1]<='9' && token[1]>='0'))
                break
            else
                fileName += "$token "
            Log.d("Filessss",token)

        } while(tokenizer.hasMoreTokens())

        fileName = fileName.trim()

//            val dirpath = path.substring(0,path.lastIndexOf("/")+1)
//
//            val imageName = fileName+System.currentTimeMillis()+".png"
//            Log.d("ImageBitmapFile", "$dirpath : $imageName")
//            val f = File(context.cacheDir, imageName)
//            var imagePath = f.absolutePath
//            Log.d("ImageBitmapFile","dir : "+f.absolutePath)
//            f.createNewFile();
////Convert bitmap to byte array
//            val retriever = MediaMetadataRetriever()
//            retriever.setDataSource(path)
//            val bitmap = retriever.getFrameAtTime(70000000)
//            val bos = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
//            val bitmapdata = bos.toByteArray()
////write the bytes in file
//            val fos = FileOutputStream(f)
//            fos.write(bitmapdata)
//            fos.flush();
//            fos.close();






        if(tokenizer.hasMoreTokens()) {
            Log.d("File_Year", token)

            var year: String = ""
            var resolution: String = ""
            year = when (token.length == 4 && token.matches("^[0-9]*$".toRegex())) {
                true -> token
                else -> ""
            }

            if (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken()
                resolution = when (token.endsWith("p")) {
                    true -> token
                    else -> ""
                }
            }
            movieDownloadFileList.add(
                MovieDownloadFile(
                    fileName,
                    path,
                    year,
                    resolution,
                    null
                )
            )
        }
        else
            movieDownloadFileList.add(
                MovieDownloadFile(
                    fileName,
                    path,
                    "0000",
                    "000",
                    null
                )
            )
    }
}