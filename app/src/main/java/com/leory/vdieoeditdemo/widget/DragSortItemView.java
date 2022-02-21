package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @Description: 可拖动view
 * @Author: leory
 * @Time: 2022/2/21
 */
public class DragSortItemView extends View {
    private int mDownX;
    private float mTranslationX;
    private Listener mListener;
    //长按的runnable
    private Runnable mLongPressRunnable = mLongPressRunnable = new Runnable() {
        @Override
        public void run() {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            isLongPress = true;
            bringToFront();
            if (mListener != null) {
                mListener.onDragStart(DragSortItemView.this);
            }
        }
    };
    //移动的阈值
    private static final int TOUCH_SLOP = 20;
    private boolean isLongPress = false;
    private boolean isMoved;

    public DragSortItemView(Context context) {
        super(context);
        init();

    }

    public DragSortItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragSortItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mTranslationX = getTranslationX();
                isMoved = false;
                postDelayed(mLongPressRunnable, 200);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMoved && (Math.abs(mDownX - x) > TOUCH_SLOP)) {
                    isMoved = true;
                    removeCallbacks(mLongPressRunnable);
                }
                if (isLongPress) {
                    int offsetX = x - mDownX;
                    setTranslationX(mTranslationX + offsetX);
                    if (mListener != null) {
                        mListener.onMove(this);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isLongPress) {
                    if (mListener != null) {
                        mListener.onDragEnd(this);
                    }
                }
                isLongPress = false;
                removeCallbacks(mLongPressRunnable);
                break;

        }
        return true;
    }

    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    public interface Listener {
        void onDragStart(View target);

        void onDragEnd(View target);

        void onMove(View target);
    }
}
