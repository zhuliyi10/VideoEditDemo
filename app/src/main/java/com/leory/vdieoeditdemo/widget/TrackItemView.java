package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.leory.vdieoeditdemo.utils.ScreenUtils;

/**
 * @Description: 轨道基本view
 * @Author: leory
 * @Time: 2020/11/9
 */
public class TrackItemView extends View {
    private static final String TAG = TrackItemView.class.getSimpleName();
    private int lastX, lastY;
    //长按的runnable
    private Runnable mLongPressRunnable;
    //移动的阈值
    private static final int TOUCH_SLOP = 20;
    private boolean isLongPress = false;
    private boolean isMoved;

    public TrackItemView(Context context) {
        super(context);
        init(context);
    }

    public TrackItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrackItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
//        setTextColor(Color.WHITE);
        setBackgroundColor(Color.RED);
        mLongPressRunnable = new Runnable() {
            @Override
            public void run() {
                isLongPress = true;
                setBackgroundColor(Color.parseColor("#7fff0000"));
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                isMoved = false;
                postDelayed(mLongPressRunnable, 300);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMoved && (Math.abs(lastX - x) > TOUCH_SLOP || Math.abs(lastY - y) > TOUCH_SLOP)) {
                    isMoved = true;
                    removeCallbacks(mLongPressRunnable);
                }
                if (isLongPress) {
                    int t = getTop();
                    int b = getBottom();
                    if(y-lastY> ScreenUtils.dp2px(getContext(),50)){

                        int offsetY = ScreenUtils.dp2px(getContext(),50);
                        t = getTop() + offsetY;
                        b = getBottom() + offsetY;
                    }
                    int offsetX = x - lastX;
                    int l = getLeft() + offsetX;
                    int r = getRight() + offsetX;
                    layout(l, t, r, b);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isLongPress = false;
                removeCallbacks(mLongPressRunnable);
                setBackgroundColor(Color.RED);
                break;

        }
        return true;
    }
}
