@file:Suppress("UNCHECKED_CAST")

package com.example.flixyts.Downloads

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flixyts.MainActivity
import com.example.flixyts.Models.MovieDownloadFile
import com.example.flixyts.R
import kotlinx.android.synthetic.main.fragment_downloads.*

class DownloadsFragment : Fragment() {

    var localFiles:ArrayList<MovieDownloadFile> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SavedInstanceState","in onCreate")
            val x = arguments?.get("localFiles")
            if (x != null)
                localFiles = x as ArrayList<MovieDownloadFile>

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d("SavedInstanceState","onCreateView")
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SavedInstanceState","onViewCreated")
        for(localFile in localFiles) {
            Log.d("MovieDownloadFile in DownloadsFragment","${localFile.getName()!!} ${localFile.getClip()}")
        }

         if (localFiles.isEmpty()) {
            Toast.makeText(context,"No Files in the folder", Toast.LENGTH_SHORT).show()
             return
        }

        val viewAdapter = DownloadsAdapter(
            localFiles.toList(),
            context!!,
            activity!!,
            activity as MainActivity
        )
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = viewAdapter
        }

    }
}
