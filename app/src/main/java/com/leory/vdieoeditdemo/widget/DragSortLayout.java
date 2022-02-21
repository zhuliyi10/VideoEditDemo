package com.leory.vdieoeditdemo.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @Description: 拖拽排序view
 * @Author: leory
 * @Time: 2022/2/21
 */
public class DragSortLayout extends FrameLayout {
    private Context mContext;
    private View mTargetView;
    private int mItemMargin;
    private int mItemWidth;
    private List<View> mChildList = new ArrayList<>();
    private ValueAnimator mScrollAnimator;
    private ValueAnimator mTransitionAnimator;
    private int mScrollX = 0;


    public DragSortLayout(Context context) {
        this(context, null);
    }

    public DragSortLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;
        for (int i = 0; i < mChildList.size(); i++) {
            View child = mChildList.get(i);
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            child.setTranslationX(childLeft);
            childLeft += child.getMeasuredWidth() + mItemMargin;
        }
    }


    private void init() {
        mItemMargin = dpToPx(mContext, 4);
        mItemWidth = dpToPx(mContext, 60);
        addDragViews();
    }

    private void addDragViews() {
        mChildList.clear();
        for (int i = 0; i < 10; i++) {
            DragSortItemView child = new DragSortItemView(mContext);
            mChildList.add(child);
            child.setBackgroundColor(getRandomColor());
            ViewGroup.LayoutParams lp = new LayoutParams(getRandomWidth(), mItemWidth);
            addView(child, lp);
            child.setListener(mItemListener);
        }
    }

    private DragSortItemView.Listener mItemListener = new DragSortItemView.Listener() {


        @Override
        public void onDragStart(View target) {
            mTargetView = target;
            onDragBegin();
        }

        @Override
        public void onDragEnd(View target) {
            cancelScrollAnim();
            mScrollX = 0;
            requestLayout();
        }

        @Override
        public void onMove(View target) {
            int targetIndex = getChildIndex(target);
            if (targetIndex == -1) return;
            judgeSwapPosition(target);
            float targetLeft = target.getTranslationX();
            if (targetLeft + mItemWidth > getMeasuredWidth()) {//大于屏幕
                startScrollAnim(false);
            } else if (targetLeft < 0) {
                startScrollAnim(true);
            } else {
                cancelScrollAnim();
            }
        }
    };

    private void onDragBegin() {
        for (int i = 0; i < mChildList.size(); i++) {
            View child = mChildList.get(i);
            if (child != mTargetView) {
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                lp.width = mItemWidth;
                child.setLayoutParams(lp);
            }
        }
        startTransitionAnim();
    }

    private int getRandomWidth() {
        double rd = Math.random();
        return (int) (mItemWidth / 3 + rd * 2 * mItemWidth);
    }

    private void judgeSwapPosition(View target) {
        int targetIndex = getChildIndex(target);
        for (int i = 0; i < mChildList.size(); i++) {
            View child = mChildList.get(i);
            if (child != target) {
                if (isDragEnter(child, target)) {
                    child.setTranslationX(getIndexLeft(targetIndex) - mScrollX);
                    Collections.swap(mChildList, targetIndex, i);
                    break;
                }
            }
        }
    }

    private void scrollList() {
        for (int i = 0; i < mChildList.size(); i++) {
            View child = mChildList.get(i);
            if (child != mTargetView) {
                child.setTranslationX(getIndexLeft(i) - mScrollX);
            }
        }
        judgeSwapPosition(mTargetView);
    }

    private void startTransitionAnim() {
        int mInitWidth = mTargetView.getMeasuredWidth();
        ViewGroup.LayoutParams lp = mTargetView.getLayoutParams();
        mTransitionAnimator = ValueAnimator.ofInt(mInitWidth, mItemWidth);
        mTransitionAnimator.setDuration(200);
        mTransitionAnimator.addUpdateListener(animation -> {
            int width = (int) animation.getAnimatedValue();
            lp.width = width;
            mTargetView.setLayoutParams(lp);
        });
        mTransitionAnimator.start();
    }

    private void startScrollAnim(boolean isLeft) {
        if (mScrollAnimator == null || !mScrollAnimator.isRunning()) {
            mScrollAnimator = ValueAnimator.ofInt(0, Integer.MAX_VALUE / 2);
            mScrollAnimator.setDuration(Integer.MAX_VALUE);
            int initScroll = mScrollX;
            mScrollAnimator.setInterpolator(new LinearInterpolator());
            mScrollAnimator.addUpdateListener(animation -> {
                int scroll = (int) animation.getAnimatedValue();
                if (isLeft) {
                    mScrollX = initScroll - scroll;
                    if (mScrollX < mItemWidth - getWidth() / 2) {
                        mScrollX = mItemWidth - getWidth() / 2;
                    }
                } else {
                    mScrollX = initScroll + scroll;
                    int lastLeft = getIndexLeft(getChildCount() - 1);
                    if (mScrollX > lastLeft - getWidth() / 2) {
                        mScrollX = lastLeft - getWidth() / 2;
                    }
                }

                Log.d("DragSortLayout", "scrollList: " + mScrollX);
                scrollList();
            });
            mScrollAnimator.start();
        }
    }

    private void cancelScrollAnim() {
        if (mScrollAnimator != null) {
            mScrollAnimator.cancel();
        }
    }

    private boolean isDragEnter(View child, View target) {
        float targetLeft = target.getTranslationX();
        float targetRight = targetLeft + mItemWidth;
        float childLeft = child.getTranslationX();
        float childRight = childLeft + mItemWidth;
        return (targetLeft > childLeft && targetLeft < childLeft + mItemWidth / 2)
                || (targetRight < childRight && targetRight > childLeft + mItemWidth / 2);
    }

    private int getIndexLeft(int childIndex) {
        return childIndex * (mItemWidth + mItemMargin);
    }

    private int getChildIndex(View child) {
        for (int i = 0; i < mChildList.size(); i++) {
            if (child == mChildList.get(i)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * dp转为px
     */
    public static int dpToPx(Context context, float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    private int getRandomColor() {
        Random random = new Random();
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < 2; i++) {
            //       result=result*10+random.nextInt(10);
            int temp = random.nextInt(16);
            r = r * 16 + temp;
            temp = random.nextInt(16);
            g = g * 16 + temp;
            temp = random.nextInt(16);
            b = b * 16 + temp;
        }
        return Color.rgb(r, g, b);
    }
}
