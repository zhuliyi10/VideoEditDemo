package com.leory.vdieoeditdemo.bean;

import androidx.annotation.NonNull;

/**
 * @Description: 轨道bean
 * @Author: leory
 * @Time: 2020/11/5
 */
public class TrackMediaBean implements Cloneable {
    private int id;//media id
    private int type;//类型
    private String name;//media name
    private long duration;//media时长
    private long atTrackTime;//在轨道的位置

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getAtTrackTime() {
        return atTrackTime;
    }

    public void setAtTrackTime(long atTrackTime) {
        this.atTrackTime = atTrackTime;
    }


    @NonNull
    @Override
    public TrackMediaBean clone() throws CloneNotSupportedException {
        TrackMediaBean bean = (TrackMediaBean) super.clone();
        return bean;
    }
}
