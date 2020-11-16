package com.leory.vdieoeditdemo.utils;

import android.content.Context;
import android.os.Vibrator;

import androidx.annotation.RequiresPermission;

import static android.Manifest.permission.VIBRATE;

/**
 * @Description: 震动相关
 * @Author: leory
 * @Time: 2020/11/16
 */
public class VibrateUtils {
    private static Vibrator vibrator;

    @RequiresPermission(VIBRATE)
    public static void shortVibrate(Context context) {
        vibrate(context, 30);
    }

    @RequiresPermission(VIBRATE)
    public static void vibrate(Context context, final long milliseconds) {
        if (vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        vibrator.vibrate(milliseconds);
    }

    @RequiresPermission(VIBRATE)
    public static void cancel() {
        if (vibrator != null) vibrator.cancel();
    }

}
