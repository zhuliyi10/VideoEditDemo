package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

import com.leory.vdieoeditdemo.bean.TrackMediaBean;

/**
 * @Description: 轨道基本view
 * @Author: leory
 * @Time: 2020/11/9
 */
public class TrackItemView extends androidx.appcompat.widget.AppCompatTextView {
    private static final String TAG = TrackItemView.class.getSimpleName();
    private int lastX, lastY;
    //长按的runnable
    private Runnable mLongPressRunnable;
    //移动的阈值
    private static final int TOUCH_SLOP = 20;
    private boolean isLongPress = false;
    private boolean isMoved;

    private TrackMediaBean bean;

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
        setTextColor(Color.WHITE);
        setBackgroundColor(Color.RED);
        mLongPressRunnable = new Runnable() {
            @Override
            public void run() {
                isLongPress = true;
                setLongPressState(true);
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
                isLongPress = false;
                postDelayed(mLongPressRunnable, 300);
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isMoved && (Math.abs(lastX - x) > TOUCH_SLOP || Math.abs(lastY - y) > TOUCH_SLOP)) {
                    isMoved = true;
                    removeCallbacks(mLongPressRunnable);
                }
                if (isLongPress) {
                    isLongPress = false;
                    getTrackView().captureChild(TrackItemView.this, event.getPointerId(0));
                }
                Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "getViewDragState: " + getTrackView().getDragHelper().getViewDragState());
                if (!isMoved && getTrackView().getDragHelper().getViewDragState() != ViewDragHelper.STATE_DRAGGING) {
                    isLongPress = false;
                    removeCallbacks(mLongPressRunnable);
                    setLongPressState(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                isLongPress = false;
                removeCallbacks(mLongPressRunnable);
                setLongPressState(false);
                break;

        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mLongPressRunnable);
        super.onDetachedFromWindow();
    }

    public void setLongPressState(boolean isLongPress) {

        if (isLongPress) {
            setBackgroundColor(Color.parseColor("#7fff0000"));
        } else {
            setBackgroundColor(Color.RED);
        }
    }

    public TrackMediaBean getBean() {
        return bean;
    }

    public void setBean(TrackMediaBean bean) {
        this.bean = bean;
        //更新视图
        setText(bean.getName());
    }

    private TrackView getTrackView() {
        return (TrackView) getParent();
    }
}
