package com.example.flixyts

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.flixyts.Home.MovieListAdapter
import com.example.flixyts.Models.Movie
import com.example.flixyts.Models.MovieListItem
import com.example.flixyts.Models.Post
import com.example.flixyts.Search.YtsAPI
import kotlinx.android.synthetic.main.activity_movie_detail.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieDetailActivity : AppCompatActivity() {

    private var viewAdapter:MovieListAdapter? = null
    lateinit var movie: Movie
    private lateinit var ytsAPI:YtsAPI
    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        if(intent.getStringExtra("from")=="MovieListAdapter") {
            val movieItem = intent.getSerializableExtra("movieItem") as MovieListItem
            val retrofit = Retrofit.Builder()
                .baseUrl("https://yts.mx/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            ytsAPI = retrofit.create(YtsAPI::class.java)
            getQuery(movieItem.imdb_code!!,"all",0)
        }else {
            movie = intent.getSerializableExtra("movie") as Movie
            updateUI()
        }

    }
    @SuppressLint("StaticFieldLeak")
    private fun getQuery(query_term:String,genre:String,minimum_rating:Int)
    {
        val call = ytsAPI.getQuery(query_term,genre,minimum_rating)
        call.enqueue(object: Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.d("YTSClient","ON FAILURE.. ${t.printStackTrace()}")
            }

            override fun onResponse(
                call: Call<Post>,
                response: Response<Post>
            ) {
                if(response.isSuccessful) {
                    Log.d("YTSClient","ON SUCCESS")
                    val post = response.body()
                    if(post==null) {
                        Log.d("YTSClient","post==null")
                        return
                    }
                    Log.d("YTSClient","status : ${post.status}")
                    Log.d("YTSClient","movie count : ${post.data.movie_count}")
                    val movies:List<Movie>? = post.data.movies

//                        for(movie in movies)
//                            Log.d("YTSClient","movie name : ${movie.title}")
                    Log.d("YTSClient","status_message : ${post.status_message}")
                    Log.d("YTSClient","@meta : ${post.meta}")
                    if(movies==null)
                    {Log.d("YTSClient","movies == NULL")
                    }
                    else
                    Log.d("YTSClient","${movies.size}")
                    movie = movies?.get(0)!!
                    updateUI()
                }
                else
                    Log.d("YTSClient",response.message())
            }
        })
    }

    fun updateUI() {
        val options = RequestOptions()
        options.centerCrop()

        movie_title.text = movie.title
        year.text = movie.year.toString()
        duration.text = movie.runtime.toString() + " mins"

        Glide.with(this)
            .load(movie.medium_cover_image)
            .apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(poster)

        for (genre in movie.genres) {
            val genreView =
                (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.genre_textview,
                    null
                ) as TextView
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            genreView.text = genre
            params.leftMargin = 8
            params.topMargin = 2
            params.bottomMargin = 2
            genreView.layoutParams = params
            genres.addView(genreView)
        }

        description.text = movie.synopsis
        rating.text = movie.rating.toString()

        youtube.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + movie.yt_trailer_code)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.google.android.youtube")
            val manager = packageManager
            val infos = manager.queryIntentActivities(intent, 0)
            if (infos.size == 0)
                Toast.makeText(this, "Currently unable to load youtube", Toast.LENGTH_SHORT).show()
            else
                startActivity(intent)
        })
        imdb.setOnClickListener(View.OnClickListener {
            try {
                val intent = Intent("android.intent.action.Main")
                intent.component =
                    ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main")
                intent.addCategory("android.intent.category.LAUNCHER")
                intent.data = Uri.parse("https://www.imdb.com/title/" + movie.imdb_code)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.imdb.com/title/" + movie.imdb_code)
                )
                startActivity(intent)
            }
        })

        download720.setOnClickListener {
            Toast.makeText(this, "Downloading 720p link", Toast.LENGTH_SHORT).show()
            for (torrent in movie.torrents) {
                if (torrent.quality == "720p") {
                    Log.d("Torrents", "quality : ${torrent.quality}")
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(torrent.url))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    intent.setPackage("com.android.chrome")
                    try{
                        startActivity(intent)
                    }
                    catch(e:ActivityNotFoundException)
                    {
                        intent.setPackage(null)
                        startActivity(intent)
                    }
                    break

                }
            }
        }
        download1080.setOnClickListener {
            Toast.makeText(this, "Downloading 1080p link", Toast.LENGTH_SHORT).show()
            for (torrent in movie.torrents) {
                if (torrent.quality == "1080p") {
                    Log.d("Torrents", "quality : ${torrent.quality}")
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(torrent.url))
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(intent)
                    intent.setPackage("com.android.chrome")
                    try{
                        startActivity(intent)
                    }
                    catch(e:ActivityNotFoundException)
                    {
                        intent.setPackage(null)
                        startActivity(intent)
                    }
                    break
                }
            }
        }
        scrapeMovieSuggestions()
    }
    @SuppressLint("StaticFieldLeak")
    fun scrapeMovieSuggestions()
    {
        val jsoup = object: AsyncTask<Void?, Void?, Void?>()
        {
            var response:Document? = null
            override fun doInBackground(vararg params: Void?): Void? {
                response = Jsoup.connect("https://www.imdb.com/title/"+movie.imdb_code+"/").get()
                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                if (response == null)
                    return
                val recommend = response?.select("div#titleRecs")!![0]
                val imdbRecommends = recommend.select("a[href^=/title/]")
                val imageRecommends = imdbRecommends.select("img[src]")
                var count = 0
                val movieItemList:MutableList<MovieListItem> = ArrayList()
                for (imdb in imdbRecommends!!) {
                    if(imdb.toString().contains("src"))
                    {
                        movieItemList.add(MovieListItem(imageRecommends[count].attr("alt"),
                                                        imdb.getElementsByTag("a")!![0].attr("href").substring(7,16),
                                                        imageRecommends[count++].attr("src")))
                    }
                    if(count==(imageRecommends.size/2))
                        break
                }
                viewAdapter = MovieListAdapter(
                    movieItemList,
                    this@MovieDetailActivity
                )
                val mLayoutManager = LinearLayoutManager(this@MovieDetailActivity,
                    LinearLayoutManager.HORIZONTAL,false)
//                    mLayoutManager.reverseLayout = true
                recommendations_list.apply {
                    layoutManager = mLayoutManager
                    adapter = viewAdapter
                }
            }
        }
        jsoup.execute()
    }
}
