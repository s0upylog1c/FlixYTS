package com.example.flixyts.Home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.flixyts.Models.MovieListItem
import com.example.flixyts.MovieDetailActivity
import com.example.flixyts.R
import org.jsoup.Jsoup
import java.lang.Exception
@SuppressLint("StaticFieldLeak")
class MovieListAdapter(private val myDataset: MutableList<MovieListItem>,private val context: Context): RecyclerView.Adapter<MovieListAdapter.ItemViewHolder>() {
    val listOfPosterLinks = MutableList<String?>(myDataset.size){null}
    val scrapingStarted = MutableList<Boolean>(myDataset.size){false}

    class ItemViewHolder(val imageView: ImageView): RecyclerView.ViewHolder(imageView){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val imageView: ImageView = LayoutInflater.from(parent.context).inflate(R.layout.item_movielist_layout,parent,false) as ImageView
        return ItemViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val options = RequestOptions()
        options.centerCrop()
//        Glide.with(context)
//            .load(myDataset[position].poster_link)
//            .apply(options)
//            .transition(DrawableTransitionOptions.withCrossFade())
//            .into(holder.imageView)
        var response:org.jsoup.nodes.Document? = null
        var poster_link:String? = null
        if(listOfPosterLinks[position]==null && !scrapingStarted[position]) {
            val fetchPoster = object : AsyncTask<Void, Void, Void>() {
                override fun onPreExecute() {
                    super.onPreExecute()
                    scrapingStarted[position]=true
                }
                override fun doInBackground(vararg params: Void?): Void? {
                    try {
                        response =
                            Jsoup.connect("https://www.imdb.com/title/${myDataset[position].imdb_code}")
                                .get()
                    } catch (e: Exception) {
                        Log.d("WEBSEARCH", e.localizedMessage!!)
                        Log.d(
                            "MovieListAdapter",
                            "https://www.imdb.com/title/${myDataset[position].imdb_code}"
                        )
                    }
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    if (response != null)
                        Log.d("MovieListAdapter", "${response.toString()} ")
                    val result = response?.select("img[alt*= Poster]")
                    if (result != null && !result.isEmpty()) {
                        myDataset[position].poster_link = result[0].attr("src")
                        listOfPosterLinks[position] = myDataset[position].poster_link
                        Glide.with(context)
                            .load(listOfPosterLinks[position])
                            .apply(options)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.imageView)

                        holder.imageView.setOnClickListener {
                            val intent = Intent(context, MovieDetailActivity::class.java)
                            intent.putExtra("from","MovieListAdapter")
                            intent.putExtra("movieItem",myDataset[position])
                            context.startActivity(intent)
                        }
                    }
//
//                Log.d("MovieListAdapter","${myDataset[position].title} :::  ${response!!.select("img[alt^="+myDataset[position].title+"]")[0].toString()}")
                }
            }
            fetchPoster.execute()
        }
        else if(listOfPosterLinks[position]!=null) {

            Glide.with(context)
                .load(myDataset[position].poster_link)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView)
        }
        else
            Glide.with(context).clear(holder.imageView)
    }

    override fun getItemCount(): Int {
      return myDataset.size
    }
}