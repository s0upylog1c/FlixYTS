package com.example.flixyts.Player

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.example.flixyts.R
import com.example.flixyts.R.drawable
import kotlinx.android.synthetic.main.activity_player.*
import java.lang.Integer.max
import java.lang.Math.min
import java.text.DecimalFormat
import kotlin.math.abs

class PlayerActivity : AppCompatActivity() {

    private var name: String? = null
    private var path: String? = null
    lateinit var mediaPlayer: MediaPlayer
    lateinit var firstSurface: SurfaceHolder
    var isPaused = true
    var handler = Handler()

    var sizeOptions:List<Pair<Int?,Int?>> = listOf( Pair(null,null), Pair(21,9), Pair(17,9), Pair(16,9), Pair(5,3), Pair(8,5), Pair(3,2), Pair(4,3), Pair(5,4), Pair(1,1))
    var currentSize = 0
    private lateinit var screenSize: ScreenSize

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
        setContentView(R.layout.activity_player)




        name = intent.getStringExtra("fileName")
        path = intent.getStringExtra("filePath")

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        surfaceView.setOnClickListener(View.OnClickListener {
            //hideNavigationBar()
        })
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                handler.removeCallbacksAndMessages(null)
                mediaPlayer.release()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                firstSurface = holder!!
                initMediaPlayer()
            }

        })

        pauseButton.setOnClickListener(View.OnClickListener { switchPlayPause() })
        sizes.setOnClickListener(View.OnClickListener { switchSizes() })
    }

    private fun initMediaPlayer() {
        if (path != null) {
            mediaPlayer = MediaPlayer.create(applicationContext, Uri.parse(path), firstSurface)

            screenSize = ScreenSize(
                rootLayout.measuredWidth,
                rootLayout.measuredHeight,
                mediaPlayer.videoWidth,
                mediaPlayer.videoHeight
            )
            setSurfaceSize(null)
            mediaPlayer.setDisplay(firstSurface)

            initSeekBar()
            attachSeekBarListener()

            surfaceGestureDetector()

            mediaPlayer.start()
        }
    }

    private fun switchPlayPause() {
        if (isPaused) {
            pauseButton.background = getDrawable(drawable.ic_pause)
            //hideNavigationBar()
            mediaPlayer.start()
        } else {
            pauseButton.background = getDrawable(drawable.ic_play)
            mediaPlayer.pause()
        }
        Log.d("MediaPlayer", "isPaused : $isPaused")
        isPaused = !isPaused
    }

    @SuppressLint("SetTextI18n")
    private fun switchSizes()
    {
        currentSize = (currentSize+1)%sizeOptions.size
        if(sizeOptions[currentSize].first==null || sizeOptions[currentSize].second==null) {
            sizes.text = "B"
            setSurfaceSize(null)
        }
        else {
            sizes.text = "${sizeOptions[currentSize].first}:${sizeOptions[currentSize].second}"
            setSurfaceSize(sizeOptions[currentSize].first!! * 1f / sizeOptions[currentSize].second!!)
        }
    }

    @SuppressLint("SetTextI18n")
    fun initSeekBar()
    {
        seekBar.max = mediaPlayer.duration/1000

        updateTime(mediaPlayer.duration/1000,totalTime)

        lateinit var update:Runnable
        update = Runnable {
            val currentPosition = mediaPlayer.currentPosition / 1000
            updateTime(currentPosition,currentTime)
            seekBar.progress = currentPosition
            handler.postDelayed(update, 1000)
        }
            handler.postDelayed(update,1000)
    }
    private fun attachSeekBarListener(){
        seekBar.setOnSeekBarChangeListener( object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser)
                    mediaPlayer.seekTo(progress*1000)
                    updateTime(progress,currentTime)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
        })
    }

    @SuppressLint("SetTextI18n")
    fun updateTime(sec:Int, textView:TextView?):String
    {
        val df = DecimalFormat("00")
        var seconds = sec
        val hours = seconds/3600
        var minutes = seconds/60
        seconds -= minutes*60
        minutes -= hours*60
        val result = "${df.format(hours)}:${df.format(minutes)}:${df.format(seconds)}"
        if(textView!=null)
        textView.text = result
        return result
    }

    private fun hideNavigationBar(){
        window.decorView.apply { systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION }
    }

    private fun setSurfaceSize(ratio:Float?)
    {
        val requiredParams = screenSize.fitScreen(ratio)
        val layoutParams = RelativeLayout.LayoutParams(requiredParams.first,requiredParams.second)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE)
        surfaceView.layoutParams = layoutParams
    }

    private fun surfaceGestureDetector()
    {
        val leftSeparator = surfaceView.width * 1f / 3
        val rightSeparator = surfaceView.width * 2f/ 3
        var action = 0
        /**
         action masks the kind of gesture that took place after onDown in mDetector
         and before onTouch() in surface OnMotionListener receives MotionEvent.ACTION_UP
         1 -> horizontal scroll
         3 -> scroll_up
         4 -> scroll_down
         */

        var e0x:Float = 0f
        var e0y:Float = 0f
        var e2x:Float = 0f
        var e2y:Float = 0f
        var progress0 = 0
        var time0 = 0
        var skipTime = 0
        val mDetector = GestureDetectorCompat(this,object: GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent?): Boolean {
                Log.d("MotionEvent","onDown $e")
                if(e!=null) {
                    e0x = e.x
                    e0y = e.y
                    progress0 = seekBar.progress
                    time0 = mediaPlayer.currentPosition
                    handler.removeCallbacksAndMessages(null)
                }
                Log.d("MotionEvent","onScroll, onDown >> e0 : $e0x $e0y")

                return true
            }

            @SuppressLint("SetTextI18n")
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Log.d("MotionEvent","onDoubleTap $e")
                if(e==null)return super.onDoubleTap(e)

                if(e.x > leftSeparator && e.x <rightSeparator) {
                    switchPlayPause()
                }
                else if(e.x > rightSeparator) {
                    mediaPlayer.seekTo(min(mediaPlayer.duration,mediaPlayer.currentPosition + 10 * 1000))
                    seekBar.progress = min(seekBar.progress+10,mediaPlayer.duration)
                    right_visuals.text = "+10s "
                    right_visuals.visibility = View.VISIBLE
                }
                else{
                    mediaPlayer.seekTo(max(0,mediaPlayer.currentPosition - 10 * 1000))
                    seekBar.progress = max(seekBar.progress-10,0)
                    left_visuals.text = "-10s "
                    left_visuals.visibility = View.VISIBLE
                }

                return super.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                if (e != null) {
                    when {
                        surfaceView.measuredHeight - buttons.measuredHeight < e.y -> {
                            buttons.visibility = View.VISIBLE
                        }
                        buttons.visibility==View.INVISIBLE -> {
                            buttons.visibility = View.VISIBLE
                        }
                        else -> {
                            buttons.visibility = View.INVISIBLE
                        }
                    }
                }
                return super.onSingleTapConfirmed(e)
            }
            @SuppressLint("SetTextI18n")
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if(e1==null || e2==null)
                    return super.onScroll(e1, e2, distanceX, distanceY)
                Log.d("MotionEvent","onScroll, e0 : $e0x $e0y")
                Log.d("MotionEvent","onScroll, e2 : ${e2.x} ${e2.y}")

                when {
                    abs(e0y-e2.y) < abs(e0x-e2.x) -> {
                        action = 1
                        e2x = e2.x
                        e2y = e2.y
                        skipTime  = (mediaPlayer.duration/3*(e2x-e0x)/surfaceView.width).toInt()
                        seekBar.progress = max(min(progress0+skipTime/1000,mediaPlayer.duration/1000),0)
                        center_visuals.visibility = View.VISIBLE
                        if(e2x>e0x)
                            center_visuals.text = "+${updateTime(skipTime/1000,null).toString().substring(3)} (${updateTime(seekBar.progress,null)})"
                        else
                            center_visuals.text = "-${updateTime(-skipTime/1000,null).toString().substring(3)} (${updateTime(seekBar.progress,null)})"
                        Log.d("Seekbar","  onScroll, progress : ${seekBar.progress}")
                    }
                    e2.y > e0y -> {
                        action = 4
                        e2x = e2.x
                        e2y = e2.y
                    }
                    abs(e0x-e2.x) < 100 -> {
                        action = 3
                        e2x = e2.x
                        e2y = e2.y
                    }
                    else -> action = 0
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onLongPress(e: MotionEvent?) {
                Log.d("MotionEvent","onLongPress $e")
                super.onLongPress(e)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d("MotionEvent","onFling $e1 $e2")
                return super.onFling(e1, e2, velocityX, velocityY)
            }

        })

        surfaceView.setOnTouchListener(View.OnTouchListener { v, event ->
            mDetector.onTouchEvent(event)

            if(event.action == MotionEvent.ACTION_UP) {
                when (action )
                {
                    1 -> {
                        //val skipTime  = ((mediaPlayer.duration/2f)*((e2x-e0x)*1f/surfaceView.width)).toInt()
                        val tempTime = mediaPlayer.currentPosition
                        val temp = seekBar.progress
                        seekBar.progress = max(min(progress0+skipTime/1000,mediaPlayer.duration/1000),0)
                        mediaPlayer.seekTo(max(min(mediaPlayer.duration,time0 + skipTime ),0))
                        Log.d("Seekbar","  onTouch, progress : ${seekBar.progress}")

                    }
                    3 -> {
                        val increase:Int = 0
                        window.attributes.screenBrightness = min(255f,window.attributes.screenBrightness+increase)
                        Toast.makeText(this,"brightness : ${window.attributes.screenBrightness}",Toast.LENGTH_SHORT).show()
                    }
                }
                center_visuals.visibility = View.INVISIBLE
                left_visuals.visibility = View.INVISIBLE
                right_visuals.visibility = View.INVISIBLE
                action = 0
                handler = Handler()
                initSeekBar()
            }
            super.onTouchEvent(event)
        })

    }
}

