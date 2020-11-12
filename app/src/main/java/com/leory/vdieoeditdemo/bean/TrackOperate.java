package com.leory.vdieoeditdemo.bean;

/**
 * @Description: 轨道操作
 * @Author: leory
 * @Time: 2020/11/12
 */
public class TrackOperate {
    public static final int TYPE_ADD=1;//添加轨道
    public static final int TYPE_SUB=2;//删除轨道
    public static final int TYPE_MOVE=3;//移动轨道
    int type;//操作类型

    public TrackOperate(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
