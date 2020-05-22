package com.example.flixyts.Search

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.flixyts.Models.Movie
import com.example.flixyts.MovieDetailActivity
import com.example.flixyts.R

class SearchAdapter(private val myDataset: MutableList<Movie>, private val context: Context) : RecyclerView.Adapter<SearchAdapter.ItemViewHolder>() {
    private var isInMyList = MutableList(myDataset.size){false}
    class ItemViewHolder(val view: View): RecyclerView.ViewHolder(view){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_search_layout,parent,false)
        return ItemViewHolder(view)
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
//        holder.textView.text = myDataset[position]
        holder.view.findViewById<TextView>(R.id.movie_name).text = myDataset[position].title
        holder.view.findViewById<TextView>(R.id.movie_year).text =
            myDataset[position].year.toString()
        holder.view.findViewById<TextView>(R.id.rating).text =
            myDataset[position].rating.toString()

        holder.view.findViewById<ImageView>(R.id.youtube).setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v="+myDataset[position].yt_trailer_code))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.google.android.youtube")
            val manager= context.packageManager
            val infos = manager.queryIntentActivities(intent,0)
            if(infos.size == 0)
                Toast.makeText(context,"Currently unable to load youtube",Toast.LENGTH_SHORT).show()
            else
                context.startActivity(intent)
        })
        holder.view.findViewById<ImageView>(R.id.imdb).setOnClickListener(View.OnClickListener {
            try
            {
                val intent = Intent("android.intent.action.Main")
                intent.component = ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main")
                intent.addCategory("android.intent.category.LAUNCHER")
                intent.data = Uri.parse("https://www.imdb.com/title/"+myDataset[position].imdb_code)
                context.startActivity(intent)
            }
            catch(e: ActivityNotFoundException) {
                val intent= Intent(Intent.ACTION_VIEW,Uri.parse("https://www.imdb.com/title/"+myDataset[position].imdb_code))
                context.startActivity(intent)
            }
        })
        val tick = holder.view.findViewById<ImageView>(R.id.tick)
        tick .setOnClickListener {
            if(isInMyList[position]) {
                tick.setBackgroundResource(R.drawable.ic_tick_disabled)
            }
            else{
                tick.setBackgroundResource(R.drawable.ic_tick_enabled)
            }
            isInMyList[position]=!isInMyList[position]

        }


        val options = RequestOptions()
        options.centerCrop()
        Glide.with(context)
            .load(myDataset[position].medium_cover_image)
            .apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.view.findViewById<ImageView>(R.id.movie_poster))

        holder.view.findViewById<ImageView>(R.id.movie_poster).setOnClickListener {
            Toast.makeText(context, myDataset[position].title, Toast.LENGTH_SHORT).show()
            val intent = Intent(context,MovieDetailActivity::class.java)
            intent.putExtra("movie",myDataset[position])
            context.startActivity(intent)
        }

    }
    override fun getItemCount() = myDataset.size

    fun clearData()
    {
        myDataset.clear()
        notifyDataSetChanged()
    }
}
