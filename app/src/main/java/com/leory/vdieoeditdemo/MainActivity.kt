package com.leory.vdieoeditdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.leory.vdieoeditdemo.listener.VideoTrackCallback
import com.leory.vdieoeditdemo.utils.TimeUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        track.addTrack(40 * 1000 )

        track.setVideoTrackCallback(object : VideoTrackCallback {
            override fun onAudioSelected(isSelected: Boolean) {
                setAudioSelected(isSelected)
            }

            override fun onPlayTime(currentMs: Long, durationMs: Long) {
                runOnUiThread {
                    val df = SimpleDateFormat("mm:ss")
                    val current = TimeUtils.millis2String(currentMs, df)
                    val duration = TimeUtils.millis2String(durationMs, df)
                    txt_time.text = "$current/$duration"
                }

            }

        })
        add.setOnClickListener {
            track.addTrack((40  * 1000 * Math.random()).toLong())
        }
        segmentation.setOnClickListener {
            track.segmentTrack()
        }
        img_back.setOnClickListener {
            track.backOperate()
        }
        img_play.setOnClickListener { track.playOrPause() }
    }

    /**
     * 设置音频是否选择
     */
    private fun setAudioSelected(isSelected: Boolean) {
        if (isSelected) {
            audio.visibility = View.GONE
            audio_select.visibility = View.VISIBLE
        } else {
            audio.visibility = View.VISIBLE
            audio_select.visibility = View.GONE
        }
    }

}