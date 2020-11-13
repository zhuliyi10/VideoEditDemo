package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

import com.leory.vdieoeditdemo.bean.TrackMediaBean;
import com.leory.vdieoeditdemo.utils.ScreenUtils;

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
    private boolean isClickSelected = false;
    private Paint selectedPaint;//选中画笔
    private Paint bgPaint;//背景画笔

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
        selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.RED);
        setTextColor(Color.WHITE);
        mLongPressRunnable = new Runnable() {
            @Override
            public void run() {
                isLongPress = true;
                setLongPressState(true);
                getTrackView().cancelChildSelected();
            }
        };
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (isClickSelected) {
            drawSelectState(canvas);
        }
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), bgPaint);
        super.onDraw(canvas);
    }

    private void drawSelectState(Canvas canvas) {
        float left = -dp2px(20);
        float right = getMeasuredWidth() - left;
        float top = -dp2px(2);
        float bottom = getMeasuredHeight() - top;
        float radius = dp2px(2);
        RectF rectF = new RectF(left, top, right, bottom);
        selectedPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(rectF, radius, radius, selectedPaint);
        float lineWidth = dp2px(2);
        float lineHeight = dp2px(10);
        selectedPaint.setColor(Color.LTGRAY);
        RectF leftRectF = new RectF(left / 2 - lineWidth / 2, getMeasuredHeight() / 2 - lineHeight / 2, left / 2 + lineWidth / 2, getMeasuredHeight() / 2 + lineHeight / 2);
        canvas.drawRect(leftRectF, selectedPaint);
        RectF rightRectF = new RectF(getMeasuredWidth() - left / 2 - lineWidth / 2, getMeasuredHeight() / 2 - lineHeight / 2, getMeasuredWidth() - left / 2 + lineWidth / 2, getMeasuredHeight() / 2 + lineHeight / 2);
        canvas.drawRect(rightRectF, selectedPaint);
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
                Log.d(TAG, "onTouchEvent: 点击事件");
                getTrackView().setChildSelected(this);
                removeCallbacks(mLongPressRunnable);
                setLongPressState(false);
                break;

        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mLongPressRunnable);
        super.onDetachedFromWindow();
    }

    /**
     * 设置是否选中
     *
     * @param isSelected
     */
    public void setClickSelected(boolean isSelected) {
        if (isSelected != isClickSelected) {
            this.isClickSelected = isSelected;
            if(isClickSelected)bringToFront();
            invalidate();
        }
    }

    /**
     * 获取是否选中
     * @return
     */
    public boolean getClickSelected(){
        return isClickSelected;
    }

    public void setLongPressState(boolean isLongPress) {
        if (isLongPress) {
            bgPaint.setColor(Color.parseColor("#7fff0000"));
        } else {
            bgPaint.setColor(Color.RED);
        }
        invalidate();
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

    private int dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }

}
