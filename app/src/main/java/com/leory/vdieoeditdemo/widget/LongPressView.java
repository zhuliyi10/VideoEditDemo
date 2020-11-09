package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

/**
 * @Description: 自定义长按
 * @Author: leory
 * @Time: 2020/11/9
 */
public class LongPressView extends View {
    public static final String TAG = LongPressView.class.getSimpleName();
    private int mLastMotionX, mLastMotionY;
    //是否移动了
    private boolean isMoved;
    //长按的runnable
    private Runnable mLongPressRunnable;
    //移动的阈值
    private static final int TOUCH_SLOP = 20;


    public LongPressView(Context context) {
        super(context);
        init();
    }

    public LongPressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LongPressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLongPressRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: 执行了长按事件");
            }
        };
    }


    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                isMoved = false;
                postDelayed(mLongPressRunnable, 200);
                break;
            case MotionEvent.ACTION_MOVE:

                if (!isMoved && (Math.abs(mLastMotionX - x) > TOUCH_SLOP || Math.abs(mLastMotionY - y) > TOUCH_SLOP)) {
                    isMoved = true;
                    removeCallbacks(mLongPressRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(mLongPressRunnable);
                break;
        }
        return true;
    }

}
