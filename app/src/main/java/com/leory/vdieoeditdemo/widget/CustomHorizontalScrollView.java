package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.customview.widget.ViewDragHelper.INVALID_POINTER;

/**
 * @Description: 自定义水平滚动视频
 * @Author: leory
 * @Time: 2021/4/19
 */
public class CustomHorizontalScrollView extends FrameLayout {
    private static final String TAG = CustomHorizontalScrollView.class.getSimpleName();
    private Context mContext;
    private float mLastX;
    private int mWidth;
    private int mViewWidth;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private int mActivePointerId = INVALID_POINTER;

    public CustomHorizontalScrollView(@NonNull Context context) {
        this(context, null);
    }

    public CustomHorizontalScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomHorizontalScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mScroller = new Scroller(mContext);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        if (getChildCount() > 0) {
            final View child = getChildAt(0);
            mViewWidth = child.getMeasuredWidth();
        }
    }
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            return true;
//        }
//        return super.onInterceptTouchEvent(event);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);
        float dx;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mLastX = event.getX();
            mActivePointerId = event.getPointerId(0);
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            dx = x - mLastX;
            Log.d(TAG, "onTouchEvent: " + dx);
            int scrollX = getScrollX();
            if (scrollX - dx < 0) {
                dx = scrollX;
            }
            if (scrollX  - dx > mViewWidth) {
                dx = scrollX  - mViewWidth;
            }
            scrollBy((int) -dx, 0);
            mLastX = x;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
            int initialVelocity = (int) mVelocityTracker.getXVelocity(mActivePointerId);
            if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                fling(-initialVelocity);
            }
            recycleVelocityTracker();
        }
        return true;
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    public void fling(int velocityX) {

        mScroller.fling(getScrollX(), 0, velocityX, 0, 0,
                mViewWidth , 0, 0);

        invalidate();

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}
