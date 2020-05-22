package com.example.flixyts

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.flixyts.Downloads.DownloadsFragment
import com.example.flixyts.Downloads.LocalSearch
import com.example.flixyts.Home.HomeFragment
import com.example.flixyts.Models.MovieDownloadFile
import com.example.flixyts.Search.SearchFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*


@Suppress("DEPRECATION", "UNCHECKED_CAST")
class MainActivity : AppCompatActivity() {
    var downloadsFragment = DownloadsFragment()
    var homeFragment = HomeFragment()
    var searchFragment = SearchFragment()
    var myListFragment = MyListFragment()
    var bundle = Bundle()
    var movieDownloadFileList: ArrayList<MovieDownloadFile> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions,0)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json = sharedPrefs.getString("movieDownloadFileList", "")
        val check = sharedPrefs.getBoolean("FilesPresent",false)
        if(!json.equals("") && check) {
            val type = object : TypeToken<List<MovieDownloadFile?>?>() {}.type
            movieDownloadFileList =
                gson.fromJson<List<MovieDownloadFile>>(json, type) as ArrayList<MovieDownloadFile>
            Log.d("SAVEDINSTANCESTATE","SHARED PREFRENCES")
            for(file in movieDownloadFileList)
                Log.d("SAVEDINSTANCESTATE","${file.getName()} :: ${file.getClip()}")

        }
        else {
            Log.d("SAVEDINSTANCESTATE", "LOCAL SEARCH : ")
            val searchResult = LocalSearch(
                Environment.getExternalStorageDirectory().absolutePath + "/Download",
                this
            )
            searchResult.searchForFiles()
            movieDownloadFileList = searchResult.getMovieDownloadFileList()

            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val editor:SharedPreferences.Editor = sharedPrefs.edit()
            editor.putBoolean("FilesPresent",movieDownloadFileList.size>0)
            editor.apply()

            for (file in movieDownloadFileList) {
                Log.d("MovieDownloadFile", "file Name : " + file.getName())
            }
        }

        var selectedFragment: Fragment = homeFragment
        openFragment(selectedFragment)
        nav_bar.setOnNavigationItemSelectedListener { item ->
            bundle = Bundle()

            when (item.itemId) {
                R.id.nav_search -> {
                    selectedFragment = searchFragment
                }
                R.id.nav_download -> {
                    selectedFragment = downloadsFragment
                    bundle.putSerializable("localFiles",movieDownloadFileList)
//                    bundle.putParcelableArray("localFiles",movieDownloadFileList)

                }
                R.id.nav_my_list -> {
                    selectedFragment = myListFragment
                }
                else -> {
                    selectedFragment = HomeFragment()
                }
            }
            openFragment(selectedFragment)
//            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,selectedFragment).commit()
            true
        }


    }
    private fun openFragment(selectedFragment:Fragment){
        selectedFragment.arguments = bundle

        val transaction:FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out)
        transaction.replace(R.id.fragment_container,selectedFragment,"BLANK_FRAGMENT").commit()
        supportFragmentManager.executePendingTransactions()
    }

    override fun onStop() {
        super.onStop()

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor:SharedPreferences.Editor = sharedPrefs.edit()
        val gson = Gson()

        val json: String = gson.toJson(movieDownloadFileList)

        editor.putString("movieDownloadFileList", json)
        editor.apply()

    }
}

