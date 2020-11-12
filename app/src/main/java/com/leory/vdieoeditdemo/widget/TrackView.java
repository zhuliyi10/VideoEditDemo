package com.leory.vdieoeditdemo.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

import com.leory.vdieoeditdemo.bean.TrackMediaBean;
import com.leory.vdieoeditdemo.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @Description: 处理轨道视频的管理和拖动
 * @Author: leory
 * @Time: 2020/11/4
 */
public class TrackView extends ViewGroup {
    private static final String TAG = TrackView.class.getSimpleName();
    private int trackId = 0;//轨道个数
    int itemTrackHeight;//一个轨道的高度
    int itemTrackViewHeight;//一个轨道view的高度
    private ViewDragHelper dragHelper;
    private int captureLeft;
    private int captureTop;
    ValueAnimator LeftEdgeAnim;//左边缘动画
    ValueAnimator rightEdgeAnim;//右边缘动画
    private int edgeTriggerSize;//边缘触发距离
    private float animSpeed;//动画速度 1ms多少个dp
    private List<List<TrackMediaBean>> trackMedias = new ArrayList<>();
    private boolean isNeedRequestLayout = false;//是否需要重新requestLayout();
    private Stack<List<List<TrackMediaBean>>> recordMedias = new Stack<>();

    public TrackView(Context context) {
        super(context);
        init(context);
    }

    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setClipToPadding(false);
        setClipChildren(false);
        itemTrackHeight = dp2px(50);
        itemTrackViewHeight = dp2px(40);
        edgeTriggerSize = dp2px(15);
        animSpeed = 1;
        post(() -> initTrackMedias());

        dragHelper = ViewDragHelper.create(this, 1f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                return false;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                Log.d(TAG, "clampViewPositionVertical: " + child);
                int bottom = getMeasuredHeight() - child.getMeasuredHeight();
                if (top < 0) top = 0;
                if (top > bottom) top = bottom;
                return top;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                int right = getMeasuredWidth() - child.getMeasuredWidth();
                if (left < 0) return 0;
                int myLeft = child.getLeft() + dx;

                Log.d(TAG, "clampViewPositionHorizontal:left=" + left + "  myLeft=" + myLeft);
                return left;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
                Log.d(TAG, "onViewDragStateChanged: " + state);
            }

            @Override
            public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
                Log.d(TAG, "onViewCaptured: " + capturedChild);
                capturedChild.bringToFront();
                super.onViewCaptured(capturedChild, activePointerId);
                captureLeft = capturedChild.getLeft();
                captureTop = capturedChild.getTop();
                getTrackContainer().requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
            }

            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {

                boolean isCanMove = isCanMove((TrackItemView) releasedChild);
                if (isCanMove) {//移动新的位置
                    moveToNewPosition((TrackItemView) releasedChild);
                } else {//不能移动，返回之前的位置
                    dragHelper.settleCapturedViewAt(captureLeft, captureTop);

                    trackMedias = copyTrackMedia(recordMedias.peek());//返回之前的操作
//                    isNeedRequestLayout = true;
//                    invalidate();
                    notifyDataChanged();

                }
                getTrackContainer().requestDisallowInterceptTouchEvent(false);
                TrackItemView itemView = (TrackItemView) releasedChild;
                itemView.setLongPressState(false);
            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }
        });
    }

    /**
     * 获取所在的行数
     *
     * @param child
     * @return
     */
    private int getRow(View child) {
        int top = child.getTop();
        return (top + itemTrackViewHeight / 2) / itemTrackHeight;
    }

    /**
     * 能否移到新的位置
     *
     * @return
     */
    private boolean isCanMove(TrackItemView child) {
        int left = child.getLeft();
        int row = getRow(child);
        if (row >= trackMedias.size()) return false;
        long leftTime = getDuration() * left / getMeasuredWidth();
        long rightTime = leftTime + child.getBean().getDuration();
        List<TrackMediaBean> data = trackMedias.get(row);
        for (TrackMediaBean bean : data) {
            if (child.getBean() == bean) continue;
            long start = bean.getAtTrackTime();
            long end = start + bean.getDuration();
            if(!(leftTime>end||rightTime<start)){//有交界
                return false;
            }
        }
        return true;
    }

    /**
     * 移动新的位置
     */
    private void moveToNewPosition(TrackItemView child) {
        int left = child.getLeft();
        int row = getRow(child);
        TrackMediaBean mediaBean = null;
        for (List<TrackMediaBean> data : trackMedias) {//先移除
            for (TrackMediaBean bean : data) {
                if (child.getBean() == bean) {
                    mediaBean = bean;
                    data.remove(bean);
                    break;
                }
            }
        }
        if (mediaBean != null) {//按时间顺序插入到所在的行
            long startTime = getDuration() * left / getMeasuredWidth();
            mediaBean.setAtTrackTime(startTime);
            List<TrackMediaBean> data = trackMedias.get(row);
            int i = 0;
            while (i < data.size()) {
                TrackMediaBean bean = data.get(i);
                if (bean.getAtTrackTime() < mediaBean.getAtTrackTime()) {
                    i++;
                } else {
                    break;
                }
            }
            data.add(i, mediaBean);
            recordToStack();
            computeTotalDuration();
            requestLayout();
        }


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (trackMedias.size() > 0) {
            for (int i = 0; i < trackMedias.size(); i++) {
                List<TrackMediaBean> data = trackMedias.get(i);
                for (int j = 0; j < data.size(); j++) {
                    TrackMediaBean mediaBean = data.get(j);
                    View child = findTrackItemView(mediaBean);
                    int specSize = (int) (width * mediaBean.getDuration() / getDuration());
                    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.EXACTLY);
                    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(itemTrackViewHeight, MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                }


            }
        }
        setMeasuredDimension(width, height);
    }


    /**
     * 找到包含mediaBean的数据
     *
     * @param mediaBean
     * @return
     */
    private TrackItemView findTrackItemView(TrackMediaBean mediaBean) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            TrackItemView child = (TrackItemView) getChildAt(i);
            if (mediaBean.getId() == child.getBean().getId()) {
                return child;
            }
        }
        return null;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {


        int height = 0;
        int padding = (itemTrackHeight - itemTrackViewHeight) / 2;
        int width = getMeasuredWidth();
        for (int i = 0; i < trackMedias.size(); i++) {
            List<TrackMediaBean> data = trackMedias.get(i);
            int t = height + padding;
            for (int j = 0; j < data.size(); j++) {
                TrackMediaBean mediaBean = data.get(j);
                View child = findTrackItemView(mediaBean);
                int l = (int) (width * mediaBean.getAtTrackTime() / getDuration());
                child.layout(l, t, l + child.getMeasuredWidth(), t + itemTrackViewHeight);

            }
            height += itemTrackHeight;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercept = dragHelper.shouldInterceptTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_UP");
                break;

        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                int scrollX = getTrackContainer().getScrollX();
                Log.d(TAG, "onTouchEvent: TrackContainer scrollX=" + scrollX);
                if (dragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING) {
                    if (x < edgeTriggerSize && scrollX != 0) {//左屏幕的滚动
                        if (LeftEdgeAnim == null || !LeftEdgeAnim.isRunning()) {
                            int startScroll = scrollX;
                            LeftEdgeAnim = ValueAnimator.ofFloat(1f, 0);
                            LeftEdgeAnim.setDuration((long) (startScroll * animSpeed));
                            int startLeft = dragHelper.getCapturedView().getLeft();
                            LeftEdgeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    float value = (float) animation.getAnimatedValue();
                                    getTrackContainer().setScrollX((int) (startScroll * value));
                                    Log.d(TAG, "onAnimationUpdate: " + value);
                                    TrackItemView child = (TrackItemView) dragHelper.getCapturedView();
                                    int moveScroll = (int) ((1 - value) * startScroll);
                                    Log.d(TAG, "onAnimationUpdate moveScroll: " + moveScroll);
                                    if (moveScroll > startLeft) {
                                        moveScroll = startLeft;
                                    }

                                    long startTime = getDuration() * (startLeft - moveScroll) / getMeasuredWidth();
                                    Log.d(TAG, "child: onTouchEvent: " + child);
                                    child.getBean().setAtTrackTime(startTime);
                                    requestLayout();
                                }
                            });
                            LeftEdgeAnim.setInterpolator(new LinearInterpolator());
                            LeftEdgeAnim.start();
                        }
                    }
                    if (ScreenUtils.getScreenWidth(getContext()) - x < edgeTriggerSize) {//右屏幕时滚动

                        if (rightEdgeAnim == null || !rightEdgeAnim.isRunning()) {//应该不会超过1000s吧
                            rightEdgeAnim = ValueAnimator.ofFloat(0, 1000000 * animSpeed);
                            rightEdgeAnim.setDuration(1000000);
                            int startLeft = dragHelper.getCapturedView().getLeft();
                            long startDuration = getDuration();
                            int startWidth = getMeasuredWidth();
                            int startScroll = scrollX;
                            rightEdgeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    float value = (float) animation.getAnimatedValue();
                                    Log.d(TAG, "onAnimationUpdate: " + value);
                                    TrackItemView child = (TrackItemView) dragHelper.getCapturedView();
                                    if (child != null) {
                                        TrackMediaBean bean = child.getBean();
                                        bean.setAtTrackTime((long) (startDuration * (startLeft + value) / startWidth));
                                        computeTotalDuration();
                                        requestLayout();
                                        getTrackContainer().setScrollX((int) (startScroll + value));
                                    }
                                }
                            });
                            rightEdgeAnim.setInterpolator(new LinearInterpolator());
                            rightEdgeAnim.start();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (LeftEdgeAnim != null && LeftEdgeAnim.isRunning()) {
                    LeftEdgeAnim.cancel();
                }
                if (rightEdgeAnim != null && rightEdgeAnim.isRunning()) {
                    rightEdgeAnim.cancel();
                }
                Log.d(TAG, "onTouchEvent: ACTION_UP");
                break;
        }
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            invalidate();
        } else if (isNeedRequestLayout) {
            isNeedRequestLayout = false;
            notifyDataChanged();
        }
    }

    public ViewDragHelper getDragHelper() {
        return dragHelper;
    }

    /**
     * 捕获子view
     *
     * @param itemView
     */
    public void captureChild(TrackItemView itemView, int activePointerId) {
        Log.d(TAG, "onViewCaptured:captureChild " + itemView);
        dragHelper.captureChildView(itemView, activePointerId);
    }


    /**
     * 添加一个轨道
     *
     * @param bean
     */
    public void addTrack(TrackMediaBean bean) {
        //初始化一个轨道的view 开始
        bean.setId(++trackId);
        bean.setName("track" + bean.getId());
        TrackItemView tv = new TrackItemView(getContext());
        tv.setText(bean.getName());
        tv.setBean(bean);
        addView(tv);
        //初始化一个item 结束

        //添加到原有的行轨道或者新行
        if (!addExitTrack(bean)) {
            List<TrackMediaBean> data = new ArrayList<>();
            data.add(bean);
            trackMedias.add(data);
        }
        recordToStack();
    }

    /**
     * 返回上次操作
     */
    public void backOperate() {
        if (recordMedias.size() > 1) {
            recordMedias.pop();
        }
        trackMedias = copyTrackMedia(recordMedias.peek());
        notifyDataChanged();

    }

    /**
     * 数据改变时重新布局,stack 出栈的时候调用
     */
    private void notifyDataChanged() {
        removeAllViews();
        for (List<TrackMediaBean> data : trackMedias) {
            for (TrackMediaBean bean : data) {
                TrackItemView tv = new TrackItemView(getContext());
                tv.setBean(bean);
                addView(tv);
            }
        }
        computeTotalDuration();
        requestLayout();
    }

    /**
     * 初始化轨道数据
     */
    private void initTrackMedias() {
        int initRows = getMeasuredHeight() / itemTrackViewHeight;
        for (int i = 0; i < initRows; i++) {
            trackMedias.add(new ArrayList<>());
        }
        recordToStack();
    }

    /**
     * 一个完整的操作完成后将数据压栈
     */
    private void recordToStack() {
        recordMedias.push(copyTrackMedia(trackMedias));
    }

    /**
     * 复制
     *
     * @param trackMedias
     * @return
     */
    private List<List<TrackMediaBean>> copyTrackMedia(List<List<TrackMediaBean>> trackMedias) {
        List<List<TrackMediaBean>> medias = new ArrayList<>();
        try {
            for (List<TrackMediaBean> data : trackMedias) {
                List<TrackMediaBean> beans = new ArrayList<>();
                for (TrackMediaBean bean : data) {
                    beans.add(bean.clone());
                }
                medias.add(beans);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return medias;
    }

    /**
     * 每一次拖动都要重新计算总时长
     * 调整总时长
     */
    private void computeTotalDuration() {
        long maxDuration = 0;
        for (List<TrackMediaBean> data : trackMedias) {
            if (data.size() > 0) {
                TrackMediaBean bean = data.get(data.size() - 1);
                long time = bean.getAtTrackTime() + bean.getDuration();//算出最后一个视频的时长，就是该行时长
                if (time > maxDuration) maxDuration = time;
            }
        }
        getTimeRuleContainer().setDuration(maxDuration);
    }


    /**
     * 尝试添加到已有的轨道行
     */
    private boolean addExitTrack(TrackMediaBean bean) {

        long startTarget = bean.getAtTrackTime();
        long endTarget = bean.getAtTrackTime() + bean.getDuration();
        for (List<TrackMediaBean> data : trackMedias) {
            if (data.size() == 0) {
                data.add(bean);
                return true;
            }
            for (int i = 0; i < data.size(); i++) {
                TrackMediaBean mediaBean = data.get(i);
                long start = mediaBean.getAtTrackTime();
                long end = mediaBean.getAtTrackTime() + mediaBean.getDuration();
                if (endTarget < start) {//在左侧，可添加
                    data.add(i, bean);
                    return true;
                } else if (startTarget > end) {//在右侧
                    if (i == data.size() - 1) {//而且右侧已经没有,则可以直接添加
                        data.add(bean);
                        return true;
                    } else {//对比同行下一个
                        continue;
                    }
                } else {//在mediaBean中间直接下行
                    break;
                }
            }
        }
        return false;
    }

    /**
     * 获取父容器
     *
     * @return
     */
    private TrackContainer getTrackContainer() {
        return (TrackContainer) getParent().getParent();
    }

    private TimeRuleContainer getTimeRuleContainer() {
        return (TimeRuleContainer) getParent();
    }

    private long getDuration() {//视频总时长
        long duration = getTimeRuleContainer().getDuration();
        return duration;
    }

    private int dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }

    private float sp2px(float dp) {
        return ScreenUtils.sp2px(getContext(), dp);
    }
}
