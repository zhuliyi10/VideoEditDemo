package com.leory.vdieoeditdemo.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.leory.vdieoeditdemo.utils.ScreenUtils;

/**
 * @Description: 可拖动view
 * @Author: leory
 * @Time: 2020/11/4
 */
public class DragView extends View {
    private int lastX, lastY,parentWidth,parentHeight;
    //长按的runnable
    private Runnable mLongPressRunnable;
    //移动的阈值
    private static final int TOUCH_SLOP = 20;
    private boolean isLongPress=false;
    private boolean isMoved;
    public DragView(Context context) {
        super(context);
        init();

    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        Point outSize = new Point();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRealSize(outSize);
        parentWidth = outSize.x;
        parentHeight = outSize.y;
        mLongPressRunnable=new Runnable() {
            @Override
            public void run() {
                isLongPress=true;
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
                isMoved=false;
                postDelayed(mLongPressRunnable, 200);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMoved && (Math.abs(lastX - x) > TOUCH_SLOP || Math.abs(lastY - y) > TOUCH_SLOP)) {
                    isMoved = true;
                    removeCallbacks(mLongPressRunnable);
                }
                if(isLongPress) {
                    int offsetX = x - lastX;
                    int offsetY = y - lastY;
                    int l = getLeft() + offsetX;
                    int r = getRight() + offsetX;
                    int t = getTop() + offsetY;
                    int b = getBottom() + offsetY;
                    if (l < 0) {
                        l = 0;
                        r = getWidth();
                    }
                    if (t < 0) {
                        t = 0;
                        b = t + getHeight();
                    }
                    if (r > parentWidth) {
                        r = parentWidth;
                        l = r - getWidth();
                    }
                    if (b > parentHeight) {
                        b = parentHeight;
                        t = b - getHeight();
                    }
                    layout(l, t, r, b);
                    lastX = x;
                    lastY = y;
                }
                break;

                case MotionEvent.ACTION_UP:
                    isLongPress=false;
                    removeCallbacks(mLongPressRunnable);
                    break;

        }
        return true;
    }
}
