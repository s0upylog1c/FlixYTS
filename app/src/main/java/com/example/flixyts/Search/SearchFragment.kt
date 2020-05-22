package com.example.flixyts.Search

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flixyts.Models.Movie
import com.example.flixyts.Models.Post
import com.example.flixyts.R
import kotlinx.android.synthetic.main.fragment_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchFragment : Fragment() {


    private var genre_selected:String = "All"
    private var ratings_selected:String = "0"
    private var text_searched = "0"
    private lateinit var ytsAPI: YtsAPI
    private var viewAdapter:SearchAdapter? = null
    //private val webSearch:WebSearch = WebSearch(WeakReference(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { text_searched = s.toString() }
        })

        ArrayAdapter.createFromResource(
            context!!,R.array.genre_values,R.layout.search_spinner_item).also {
            adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genre.adapter = adapter
        }

        genre.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            @SuppressLint("DefaultLocale")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (parent != null) {
                    genre_selected = parent.getItemAtPosition(position).toString().toLowerCase()
                    Log.d("Genre","Genre Selected : ${parent.getItemAtPosition(position)}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("Genre","No Genre Selected")
            }
        }

        ArrayAdapter.createFromResource(
            context!!,R.array.rating_values,R.layout.search_spinner_item).also {
                adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ratings.adapter = adapter
        }

        ratings.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                ratings_selected = if(parent!!.getItemAtPosition(position).toString() == "All")
                    "0"
                else
                    parent.getItemAtPosition(position).toString().substring(0,1)
                Log.d("Ratings","Rating Selected : ${parent.getItemAtPosition(position)}")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("Ratings","No Ratings Selected")
            }
        }

        searchButton.setOnClickListener {


            val retrofit = Retrofit.Builder()
                .baseUrl("https://yts.mx/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            ytsAPI = retrofit.create(YtsAPI::class.java)
            getQuery(text_searched,genre_selected,ratings_selected.toInt())
        }
    }

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
                        Log.d("YTSClient","  ")
                    showSearchResult(movies)
                }
                else
                    Log.d("YTSClient",response.message())
            }
        })
    }

    fun showSearchResult(movies:List<Movie>?)
    {
        if( movies==null ||movies.isEmpty()) {
            Toast.makeText(activity,"No result found!!",Toast.LENGTH_SHORT).show()
            if(viewAdapter!=null)
                viewAdapter!!.clearData()
            return
        }
        viewAdapter = SearchAdapter(
            movies.toMutableList(),
            context!!
        )
        results.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = viewAdapter
        }

    }
}
