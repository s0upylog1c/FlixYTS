package com.example.flixyts.Home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flixyts.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webSearch = WebSearch()
        webSearch.asyncSearch("https://www.imdb.com/list/ls044087097/",top_drama_list,context!!).execute()
        webSearch.asyncSearch("https://www.imdb.com/list/ls063463658/",top_rated_list,context!!).execute()
        webSearch.asyncSearch("https://www.imdb.com/list/ls057459805/",top_horror_list,context!!).execute()
        webSearch.asyncSearch("https://www.imdb.com/list/ls094715071/",top_book_based_list,context!!).execute()
    }
}
