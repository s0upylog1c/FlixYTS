package com.example.flixyts.Home

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flixyts.Models.MovieListItem
import org.jsoup.Jsoup
import java.io.InputStream
import java.lang.Exception

class WebSearch()  {
    private var viewAdapter:MovieListAdapter? = null


    @SuppressLint("StaticFieldLeak")
    fun asyncSearch(url:String,recyclerView: RecyclerView,context: Context): AsyncTask<Void, Void, Void> {
        var response:org.jsoup.nodes.Document?=null
        var inputStream:InputStream? = null
        return object : AsyncTask<Void, Void, Void>() {

            override fun doInBackground(vararg params: Void?): Void? {
                Log.d("WEBSEARCH","doInBackground")
                try { response = Jsoup.connect(url).get() }
                catch (e:Exception)
                {Log.d("WESEARCH",e.localizedMessage!!)}
                return null
            }

            override fun onPreExecute() {
                super.onPreExecute()
                Log.d("WEBSEARCH", "OnPreExecute : ")
            }
            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                val movieItemList:MutableList<MovieListItem> = ArrayList()
                Log.d("WEBSEARCH", "OnPostExecute : ")
                if (response != null) {
                    val results = response!!.select("a[href^=/title]")
                    if(results.isEmpty()) {
                        Toast.makeText(context, "NO RESULTS FOUND",Toast.LENGTH_SHORT).show()
                        return
                    }
                    Log.d("WEBSEARCH","${results.size} RESULTS FOUND")
                    var count = 0
                    val images = results.select("img[src]")
                    var poster_link:String? = null
                    for(result in results) {
                        if (count%2 ==0){
                            Log.d("WEBSEARCH",result.select("img[src]").attr("src"))
                            poster_link = result.select("img[src]").attr("src")
//                            movieItem.poster_link = result.select("img[src]").attr("src")
                        }
                        else {
//                            movieItem.title = result.text()
//                            movieItem.imdb_code = result.attr("href").substring(7,14)
                            movieItemList.add(MovieListItem(result.text(),result.attr("href").substring(7,16),poster_link))
                        }
                        count++
                        if(count>20)
                            break;
                    }

                    viewAdapter = MovieListAdapter(
                        movieItemList,
                        context
                    )
                    val mLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
//                    mLayoutManager.reverseLayout = true
                    recyclerView.apply {
                        layoutManager = mLayoutManager
                        adapter = viewAdapter
                    }
                }
            }
        }
    }
}