package com.leory.vdieoeditdemo.listener;

/**
 * @Description: 视频轨道回调
 * @Author: leory
 * @Time: 2020/11/16
 */
public interface VideoTrackCallback {
    void onAudioSelected(boolean isSelected);//音频的选中和取消
    void onPlayTime(long currentMs,long durationMs);//实时更新
}
