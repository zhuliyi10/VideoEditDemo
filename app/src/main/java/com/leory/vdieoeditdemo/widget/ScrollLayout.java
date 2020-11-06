package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * @Description: 可滚动布局
 * @Author: leory
 * @Time: 2020/11/4
 */
public class ScrollLayout extends LinearLayout {
    private int lastX, lastY;
    private int lastDownX,lastDownY;

    public ScrollLayout(Context context) {
        super(context);
        init();

    }

    public ScrollLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                lastDownX=x;
                lastDownY=y;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = x - lastX;
                int dy = y - lastY;
                scrollBy(-dx,-dy);
                invalidate();
                lastX=x;
                lastY=y;

                break;
                case MotionEvent.ACTION_UP:
                    View child=getChildAt(0);
                    if(child==null)return true;
                    int[] location=new int[2];
                    child.getLocationOnScreen(location);
                    int centerX=location[0]+child.getWidth()/2;
                    int centerY=location[1]+child.getHeight()/2;
                    if(centerX<getLeft()||centerX>getRight()||centerY<getTop()||centerY>getBottom()){
                        scrollBy(x-lastDownX,y-lastDownY);
                        invalidate();
                    }
                    break;

        }
        return true;
    }
}
