package com.leory.vdieoeditdemo

import android.os.Bundle
import android.view.GestureDetector
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.leory.vdieoeditdemo.bean.TrackMediaBean
import com.leory.vdieoeditdemo.widget.TrackContainer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        track.addTrack(40*1000*1000)
        add.setOnClickListener {
            track.addTrack((40*1000*1000*Math.random()).toLong())
        }

    }

}