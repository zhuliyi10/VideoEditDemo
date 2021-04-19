package com.leory.vdieoeditdemo.utils;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;

/**
 * @Description: 时间转化工具
 * @Author: leory
 * @Time: 2020/12/1
 */
public class TimeUtils {
    /**
     * Milliseconds to the formatted time string.
     *
     * @param millis The milliseconds.
     * @param format The format.
     * @return the formatted time string
     */
    public static String millis2String(final long millis, @NonNull final DateFormat format) {
        return format.format(new Date(millis));
    }
}
