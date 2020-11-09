package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.HorizontalScrollView;

import androidx.core.view.GestureDetectorCompat;

import com.leory.vdieoeditdemo.bean.TrackMediaBean;
import com.leory.vdieoeditdemo.utils.ScreenUtils;

/**
 * @Description: 轨道顶级Container
 * @Author: leory
 * @Time: 2020/11/4
 */
public class TrackContainer extends HorizontalScrollView {

    public static final String TAG = TrackContainer.class.getSimpleName();
    private float baseLineWidth = 0f;//基准线宽度
    private Paint baseLinePaint;
    private float scaleFactor = 1f, lastScaleFactor = 1f;
    private ScaleGestureDetector scaleGestureDetector;//缩放监听器
    private GestureDetectorCompat gestureDetector;
    private TrackView trackView;
    private boolean isDoublePointer = false;
    private long lastScaleTime=0L;//上一次缩放的时间

    public TrackContainer(Context context) {
        super(context);
        init(context);
    }

    public TrackContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrackContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setClipToPadding(false);
        setClipChildren(false);
        setBackgroundColor(Color.BLACK);
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        baseLineWidth = dp2px(2f);
        baseLinePaint = new Paint();
        baseLinePaint.setAntiAlias(true);
        baseLinePaint.setColor(Color.WHITE);
        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        trackView = new TrackView(context);
        addView(trackView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: ACTION_UP");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isDoublePointer = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                isDoublePointer = false;
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_UP");
                lastScaleTime=System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                if(isDoublePointer&scaleGestureDetector.onTouchEvent(ev))return true;
                if(System.currentTimeMillis()-lastScaleTime<200)return true;//缩放后200ms不做滚动

        }
        scaleGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setPadding(width / 2, 0, width / 2, 0);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        float left = getScrollX() + (getMeasuredWidth() - baseLineWidth) / 2;
        canvas.drawRect(
                left,
                0,
                left + baseLineWidth,
                getMeasuredHeight(),
                baseLinePaint

        );
//        Log.d(TAG, "getScrollX: " + getScrollX());
//        Log.d(TAG, "getMeasuredWidth: " + getMeasuredWidth());
    }

    public void addTrack(long time) {
        TrackMediaBean bean = new TrackMediaBean();
        bean.setDuration(time);
        trackView.addTrack(bean);
    }

    /**
     * 获取当前的进度
     *
     * @return
     */
    public float getProgress() {
        return getScaleX() / (trackView.getMeasuredWidth() - getMeasuredWidth());
    }

    private float dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }

    private ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor =  detector.getScaleFactor() * lastScaleFactor;
            log("onScale:" + scaleFactor);
            log("getScaleFactor:" + detector.getScaleFactor());
            if (scaleFactor > 12f) {
                scaleFactor = 12f;
            }
            if (scaleFactor < 1 / 20f) {
                scaleFactor = 1 / 20f;
            }
            trackView.setScale(scaleFactor);
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaleFactor = 1f;
            log("onScaleBegin:");
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            lastScaleFactor = scaleFactor;
            log("onScaleEnd:");
        }
    };

    private void log(String log) {
        Log.d(TAG, log);
    }
}
