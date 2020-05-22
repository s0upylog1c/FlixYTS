package com.example.flixyts.Downloads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.flixyts.MainActivity
import com.example.flixyts.Models.MovieDownloadFile
import com.example.flixyts.Player.PlayerActivity
import com.example.flixyts.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class DownloadsAdapter(private val myDataset: List<MovieDownloadFile>, private val context: Context, private val activity: Activity,private var mainActivity: MainActivity) : RecyclerView.Adapter<DownloadsAdapter.ItemViewHolder>() {
    class ItemViewHolder(val view: View): RecyclerView.ViewHolder(view){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_downloads_layout,parent,false)
        return ItemViewHolder(view)
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
//        holder.textView.text = myDataset[position]
        holder.view.findViewById<TextView>(R.id.movie_name).text = myDataset[position].getName()
        holder.view.findViewById<TextView>(R.id.movie_year).text = myDataset[position].getYear()
        holder.view.findViewById<TextView>(R.id.movie_resolution).text = myDataset[position].getResolution()

        val options = RequestOptions()
        options.centerCrop()
        if(holder.view.findViewById<ImageView>(R.id.movie_clip).background!=null &&myDataset[position].getClip()!=null) {

            Glide.with(context)
                .load(File(myDataset[position].getClip()!!)) // Uri of the picture
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.view.findViewById<ImageView>(R.id.movie_clip))

            Log.d(
                "SavedInstanceState",
                "In Downloads Adapter :: ${myDataset[position].getName()} : ${myDataset[position].getClip()}"
            )


            holder.view.findViewById<ImageView>(R.id.movie_clip).setOnClickListener {
                Toast.makeText(context, myDataset[position].getName(), Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    activity,
                    PlayerActivity::class.java
                )
                intent.putExtra("fileName", myDataset[position].getName())
                intent.putExtra("filePath", myDataset[position].getPath())
                startActivity(context, intent, null)
            }
        }
        else if( ! myDataset[position].isAsyncStarted())
        {
            Log.d("SavedInstanceState","ASYNC SAVE BITMAP POSITION EXECUTE!!!")
           asyncSaveBitmap(position,holder.view.findViewById<ImageView>(R.id.movie_clip)).execute()
        }
        else
            Glide.with(context).clear(holder.view.findViewById<ImageView>(R.id.movie_clip))
    }

    override fun getItemCount() = myDataset.size





    @SuppressLint("StaticFieldLeak")
    fun asyncSaveBitmap(position:Int,imageView: ImageView):AsyncTask<Void,String,Void> {

        val task:AsyncTask<Void,String,Void> = object: AsyncTask<Void,String,Void>(){

            lateinit var  imagePath:String

            override fun doInBackground(vararg params: Void?): Void? {
                // ...
                val path = myDataset[position].getPath()
                val dirpath = path?.substring(0,path.lastIndexOf("/")+1)
                val fileName = myDataset[position].getName()

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)
                var time = 70000000L
                var bitmap = retriever.getFrameAtTime(time)
                while(isImageDark(bitmap)) {
                    bitmap = retriever.getFrameAtTime(time)
                    time+=60000000
                }
                Log.d("Palette", "FileName : $fileName")
                Log.d("Palette", "IsDark : " + isImageDark(bitmap))

                val imageName = fileName+System.currentTimeMillis()+".png"
                Log.d("ImageBitmapFile", "$dirpath : $imageName")
                val f = File(context.cacheDir, imageName)
                imagePath = f.absolutePath
                Log.d("ImageBitmapFile","dir : "+f.absolutePath)
                f.createNewFile();
//Convert bitmap to byte array
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 , bos)
                val bitmapdata = bos.toByteArray()
//write the bytes in file
                val fos = FileOutputStream(f)
                fos.write(bitmapdata)
                fos.flush();
                fos.close();

                return null
            }

            override fun onPreExecute() {
                super.onPreExecute()
                myDataset[position].setAsyncStarted(true)
                // ...
            }

            @SuppressLint("CheckResult")
            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                myDataset[position].setMovieClip(imagePath)
                mainActivity.movieDownloadFileList[position]?.setMovieClip(imagePath)

                myDataset[position].setAsyncEnded(true)

                val options = RequestOptions()
                options.centerCrop()
                Glide.with(context)
                    .load(File(myDataset[position].getClip()!!)) // Uri of the picture
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)

                notifyDataSetChanged()
                // ...
            }
        }
        return task
    }

    fun isImageDark(bitmap:Bitmap):Boolean{
        var dark = false

        val darkThreshold = bitmap.width*bitmap.height*0.55f;
        var darkPixels=0;

         val pixels:IntArray? = IntArray(bitmap.width*bitmap.height)
        bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());

        if(pixels==null) return false

        for( pixel in pixels){
            val color = pixel;
            val r = Color.red(color);
            val g = Color.green(color);
            val b = Color.blue(color);
            val luminance = (0.299*r+0.0f + 0.587*g+0.0f + 0.114*b+0.0f)
            if (luminance<50) {
                darkPixels++;
            }
        }

        if (darkPixels >= darkThreshold) {
            dark = true;
        }
        return dark
    }

}
