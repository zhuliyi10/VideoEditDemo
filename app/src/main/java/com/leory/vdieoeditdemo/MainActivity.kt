package com.leory.vdieoeditdemo

import android.os.Bundle
import android.view.GestureDetector
import android.view.View
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
        track.setVideoTrackCallback {
            setAudioSelected(it)
        }
        add.setOnClickListener {
            track.addTrack((40*1000*1000*Math.random()).toLong())
        }
        segmentation.setOnClickListener {
            track.segmentTrack()
        }
        img_back.setOnClickListener {
            track.backOperate()
        }
    }

    /**
     * 设置音频是否选择
     */
    private fun setAudioSelected(isSelected:Boolean){
        if(isSelected){
            audio.visibility=View.GONE
            audio_select.visibility=View.VISIBLE
        }else{
            audio.visibility=View.VISIBLE
            audio_select.visibility=View.GONE
        }
    }

}