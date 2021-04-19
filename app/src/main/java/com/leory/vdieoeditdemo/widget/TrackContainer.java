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

import com.leory.vdieoeditdemo.bean.TrackMediaBean;
import com.leory.vdieoeditdemo.listener.VideoTrackCallback;
import com.leory.vdieoeditdemo.utils.ScreenUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Description: 轨道顶级Container
 * @Author: leory
 * @Time: 2020/11/4
 */
public class TrackContainer extends HorizontalScrollView {

    private boolean isPlaying = false;
    public static final String TAG = TrackContainer.class.getSimpleName();
    private float baseLineWidth = 0f;//基准线宽度
    private Paint baseLinePaint;
    private float scaleFactor = 1f, lastScaleFactor = 1f;
    private ScaleGestureDetector scaleGestureDetector;//缩放监听器
    private TimeRuleContainer timeRuleContainer;
    private boolean isDoublePointer = false;//是否双指按下
    private long lastScaleTime = 0L;//上一次缩放的时间
    private VideoTrackCallback callback;
    private Timer mTimer = new Timer();
    private int timeCount = 0;
    private static final long PLAY_VIDEO_REFRESH_TIME = 2;//多少毫秒刷新一次

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
        timeRuleContainer = new TimeRuleContainer(context);
        addView(timeRuleContainer);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        updateTime();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        updateTime();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                stopPlay();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isDoublePointer = true;
                Log.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_DOWN ");
                return true;

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean scale = scaleGestureDetector.onTouchEvent(ev);
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                isDoublePointer = true;

                Log.d(TAG, "onTouchEvent: ACTION_POINTER_DOWN ");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                isDoublePointer = false;
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_UP ");
                lastScaleTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDoublePointer && scale) return true;
                long diff = System.currentTimeMillis() - lastScaleTime;
                if (diff < 50) return true;//缩放后50ms不做滚动
                Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                break;

        }

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
        //画中间线
        canvas.drawRect(
                left,
                0,
                left + baseLineWidth,
                getMeasuredHeight(),
                baseLinePaint

        );
    }

    /**
     * 添加轨道数据
     *
     * @param time
     */
    public void addTrack(long time) {
        TrackMediaBean bean = new TrackMediaBean();
        bean.setDuration(time);
        timeRuleContainer.addTrack(bean);
    }

    /**
     * 返回上次操作
     */
    public void backOperate() {
        if (getTrackView() != null) {
            getTrackView().backOperate();
        }
    }

    /**
     * 播放或暂停
     */
    public void playOrPause() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            timeCount = 0;
            int lastScrollX = getScrollX();
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timeCount++;
                    long playTime = timeCount * PLAY_VIDEO_REFRESH_TIME;
                    long durationMs = getRuleContainer().getDuration();
                    int width = getRuleContainer().getMeasuredWidth();
                    int deltaWidth = (int) (playTime * width / durationMs);
                    int newScrollX = lastScrollX + deltaWidth;
                    if (newScrollX > width) {
                        newScrollX = width;
                        stopPlay();
                    }
                    setScrollX(newScrollX);
                }
            }, PLAY_VIDEO_REFRESH_TIME, PLAY_VIDEO_REFRESH_TIME);
        } else {
            mTimer.cancel();
        }
    }

    private void stopPlay() {
        Log.d(TAG, "stopPlay: ");
        isPlaying = false;
        mTimer.cancel();

    }

    /**
     * 操作回调更新页面
     *
     * @param callback
     */
    public void setVideoTrackCallback(VideoTrackCallback callback) {
        this.callback = callback;
        if (getTrackView() != null) {
            getTrackView().setVideoTrackCallback(callback);
        }
    }

    /**
     * 分割轨道
     */
    public void segmentTrack() {
        if (getTrackView() != null) {
            getTrackView().segmentTrack();
        }
    }

    /**
     * 获取时间刻度容器
     *
     * @return
     */
    public TimeRuleContainer getRuleContainer() {
        return timeRuleContainer;
    }

    /**
     * 获取轨道trackView
     *
     * @return
     */
    private TrackView getTrackView() {
        if (timeRuleContainer != null) return timeRuleContainer.getTrackView();
        return null;
    }

    /**
     * 更新时间
     */
    private void updateTime() {
        if (!isDoublePointer) {
            getRuleContainer().setCurrentTime();
            if (callback != null) {
                long durationMs = getRuleContainer().getDuration();
                long currentMs = durationMs * getScrollX() / getRuleContainer().getMeasuredWidth();
                callback.onPlayTime(currentMs, durationMs);
            }
        }
    }

    private float dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }

    private void log(String log) {
        Log.d(TAG, log);
    }

    private ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor = (float) (Math.pow(detector.getScaleFactor(), 2) * lastScaleFactor);
            if (scaleFactor > 12f) {
                scaleFactor = 12f;
            }
            if (scaleFactor < 1 / 20f) {
                scaleFactor = 1 / 20f;
            }
            timeRuleContainer.setScale(scaleFactor);
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaleFactor = 1f;

            Log.d(TAG, "onScaleBegin: " + isDoublePointer);
            timeRuleContainer.startScale(true);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d(TAG, "onScaleEnd: " + isDoublePointer);
            lastScaleFactor = scaleFactor;
            timeRuleContainer.startScale(false);
        }
    };


}
