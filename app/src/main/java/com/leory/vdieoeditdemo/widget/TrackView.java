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
import com.leory.vdieoeditdemo.listener.VideoTrackCallback;
import com.leory.vdieoeditdemo.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @Description: 处理轨道数据的管理和拖动
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
    private List<TrackMediaBean> trackMedias = new ArrayList<>();
    private boolean isNeedRequestLayout = false;//是否需要重新requestLayout();
    private Stack<List<TrackMediaBean>> recordMedias = new Stack<>();
    int lastY;
    int suckDistance;//自动吸住最小距离
    VideoTrackCallback callback;

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
        itemTrackHeight = dp2px(46);
        itemTrackViewHeight = dp2px(36);
        edgeTriggerSize = dp2px(15);
        suckDistance = dp2px(4);
        animSpeed = 1;
        post(() -> initTrackMedias());

        dragHelper = ViewDragHelper.create(this, 1f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                return false;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                Log.d(TAG, "clampViewPositionVertical: top=" + top);
                Log.d(TAG, "clampViewPositionVertical: dy=" + dy);
//                int bottom = getMeasuredHeight() - child.getMeasuredHeight();
//                if (top < 0) top = 0;
//                if (top > bottom) top = bottom;
//                return top;
                return top - dy;
            }

            @Override
            public int clampViewPositionHorizontal(View view, int left, int dx) {
                if (left < 0) return 0;
//                int childLeft = left;
//                int childRight=left+view.getMeasuredWidth();
//                Log.d(TAG, "clampViewPositionHorizontal: dx "+dx);
//                for (int i = 0; i < getChildCount(); i++) {
//                    View child = getChildAt(i);
//                    if (child == view) continue;
//                    int l = child.getLeft();
//                    int r = child.getRight();
//                    if (Math.abs(l - childLeft) < suckDistance) {
//                        left = l;
//                    }
//                    if (Math.abs(r - childLeft) < suckDistance) {
//                        left = r;
//                    }
//                    if (Math.abs(l - childRight) < suckDistance) {
//                        left = l - view.getMeasuredWidth();
//                    }
//                    if (Math.abs(r - childRight) < suckDistance) {
//                        left = r - view.getMeasuredWidth();
//                    }
//
//                }
                return left;
            }

            @Override
            public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
                Log.d(TAG, "onViewCaptured: " + capturedChild);
                capturedChild.bringToFront();
                super.onViewCaptured(capturedChild, activePointerId);
                captureLeft = capturedChild.getLeft();
                captureTop = capturedChild.getTop();
                getParent().requestDisallowInterceptTouchEvent(true);
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
                    isNeedRequestLayout = true;
                    TrackMediaBean mediaBean = ((TrackItemView) releasedChild).getBean();
                    long startTime = getDuration() * captureLeft / getMeasuredWidth();
                    int row = getRow(captureTop);
                    mediaBean.setAtTrackTime(startTime);
                    mediaBean.setRow(row);
                    invalidate();

                }
                getTrackContainer().requestDisallowInterceptTouchEvent(false);
                getParent().requestDisallowInterceptTouchEvent(false);
                TrackItemView itemView = (TrackItemView) releasedChild;
                itemView.setLongPressState(false);
            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return 1;
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return 1;
            }
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        for (TrackMediaBean mediaBean : trackMedias) {
            View child = mediaBean.getItemView();
            int specSize = (int) (width * mediaBean.getDuration() / getDuration());
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(itemTrackViewHeight, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
        setMeasuredDimension(width, getTotalRow() * itemTrackHeight);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int padding = (itemTrackHeight - itemTrackViewHeight) / 2;
        int width = getMeasuredWidth();
        for (TrackMediaBean mediaBean : trackMedias) {
            View child = mediaBean.getItemView();
            int l = (int) (width * mediaBean.getAtTrackTime() / getDuration());
            int t = padding + mediaBean.getRow() * itemTrackHeight;
            child.layout(l, t, l + child.getMeasuredWidth(), t + itemTrackViewHeight);

        }

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercept = dragHelper.shouldInterceptTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_DOWN");
                lastY = (int) ev.getRawY();
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
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = y - lastY;
                Log.d(TAG, "onTouchEvent: dy=" + dy);
                int scrollX = getTrackContainer().getScrollX();
                if (dragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING) {
                    TrackItemView child = (TrackItemView) dragHelper.getCapturedView();
                    TrackMediaBean mediaBean = child.getBean();
                    if (dy > itemTrackHeight) {
                        lastY = y;
                        mediaBean.setRow(mediaBean.getRow() + 1);
                        long startTime = getDuration() * child.getLeft() / getMeasuredWidth();
                        mediaBean.setAtTrackTime(startTime);
                        requestLayout();
                    } else if (dy < -itemTrackHeight) {
                        lastY = y;
                        if (mediaBean.getRow() > 0) {
                            mediaBean.setRow(mediaBean.getRow() - 1);
                        }
                        long startTime = getDuration() * child.getLeft() / getMeasuredWidth();
                        mediaBean.setAtTrackTime(startTime);
                        requestLayout();
                    }
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
                    } else if (ScreenUtils.getScreenWidth(getContext()) - x < edgeTriggerSize) {//右屏幕时滚动

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
                cancelAnim();
                Log.d(TAG, "onTouchEvent: ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_UP:
                cancelAnim();
                cancelChildSelected();
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
            requestLayout();
        }
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
        tv.setBean(bean);
        bean.setItemView(tv);
        //添加到原有的行轨道或者新行
        int row = getInsertRow(bean);
        trackMedias.add(bean);
        addView(tv);
        //初始化一个item 结束

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
     * 操作回调
     *
     * @param callback
     */
    public void setVideoTrackCallback(VideoTrackCallback callback) {
        this.callback = callback;
    }

    /**
     * 分割轨道
     */
    public void segmentTrack() {
        TrackItemView selectedView = getSelectedChild();
        if (selectedView != null) {
            long currentTime = getDuration() * getTrackContainer().getScrollX() / getMeasuredWidth();
            TrackMediaBean bean = selectedView.getBean();
            long endTime = bean.getAtTrackTime() + bean.getDuration();
            if (currentTime > bean.getAtTrackTime() && currentTime < endTime) {//可以切割
                try {
                    TrackMediaBean newBean = bean.clone();
                    newBean.setId(++trackId);
                    newBean.setDuration(currentTime - newBean.getAtTrackTime());
                    TrackItemView tv = new TrackItemView(getContext());
                    tv.setBean(newBean);
                    newBean.setItemView(tv);
                    trackMedias.add(newBean);
                    addView(tv);
                    bean.setAtTrackTime(currentTime);
                    bean.setDuration(endTime - currentTime);
                    requestLayout();
                    recordToStack();
                    selectedView.bringToFront();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 取消子view的选择
     */
    protected void cancelChildSelected() {
        if (callback != null) {
            callback.onAudioSelected(false);
        }
        for (int i = 0; i < getChildCount(); i++) {
            TrackItemView child = (TrackItemView) getChildAt(i);
            child.setClickSelected(false);
        }
    }

    /**
     * 选中某个view
     *
     * @param target
     */
    protected void setChildSelected(TrackItemView target) {
        if (callback != null) {
            callback.onAudioSelected(true);
        }
        for (TrackMediaBean mediaBean : trackMedias) {
            mediaBean.getItemView().setClickSelected(target == mediaBean.getItemView());
        }
    }

    /**
     * 取消动画
     */
    private void cancelAnim() {
        if (LeftEdgeAnim != null && LeftEdgeAnim.isRunning()) {
            LeftEdgeAnim.cancel();
        }
        if (rightEdgeAnim != null && rightEdgeAnim.isRunning()) {
            rightEdgeAnim.cancel();
        }
    }

    protected ViewDragHelper getDragHelper() {
        return dragHelper;
    }

    /**
     * 捕获子view
     *
     * @param itemView
     */
    protected void captureChild(TrackItemView itemView, int activePointerId) {
        Log.d(TAG, "onViewCaptured:captureChild " + itemView);
        dragHelper.captureChildView(itemView, activePointerId);
    }


    /**
     * 数据改变时重新布局,stack 出栈的时候调用
     */
    private void notifyDataChanged() {
        removeAllViews();
        for (TrackMediaBean bean : trackMedias) {
            TrackItemView tv = new TrackItemView(getContext());
            tv.setBean(bean);
            bean.setItemView(tv);
            addView(tv);
        }
        computeTotalDuration();
        requestLayout();
    }

    /**
     * 初始化轨道数据
     */
    private void initTrackMedias() {
        trackMedias = new ArrayList<>();
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
    private List<TrackMediaBean> copyTrackMedia(List<TrackMediaBean> trackMedias) {
        List<TrackMediaBean> medias = new ArrayList<>();
        try {
            for (TrackMediaBean bean : trackMedias) {
                medias.add(bean.clone());
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
        for (TrackMediaBean bean : trackMedias) {
            long time = bean.getAtTrackTime() + bean.getDuration();
            if (time > maxDuration) maxDuration = time;
        }
        getTimeRuleContainer().setDuration(maxDuration);
    }

    /**
     * 插入的行数
     *
     * @param target
     * @return
     */
    private int getInsertRow(TrackMediaBean target) {
        Map<Integer, Integer> map = new HashMap<>();//所在时间已经交叉row,即不能插入行数
        long targetLeft = target.getAtTrackTime();
        long targetRight = targetLeft + getDuration();
        for (TrackMediaBean mediaBean : trackMedias) {
            long left = mediaBean.getAtTrackTime();
            long right = left + mediaBean.getDuration();
            if (!(targetLeft > right || targetRight < left)) {//相交
                map.put(mediaBean.getRow(), 0);
            }
        }
        Set<Integer> keys = map.keySet();
        int row = getTotalRow();
        for (int i = 0; i < row; i++) {
            if (!keys.contains(i)) {
                row = i;
                break;
            }
        }
        target.setRow(row);
        return row;
    }

    /**
     * 获取行数
     *
     * @return
     */
    private int getTotalRow() {
        int maxRow = 0;
        for (TrackMediaBean mediaBean : trackMedias) {
            if (mediaBean.getRow() > maxRow) {
                maxRow = mediaBean.getRow();
            }
        }
        if (maxRow < 4) return 4;
        else return maxRow + 1;
    }

    /**
     * 获取所在的行数
     *
     * @return
     */
    private int getRow(int top) {
        return (top + itemTrackViewHeight / 2) / itemTrackHeight;
    }

    /**
     * 获取选中的view
     */
    private TrackItemView getSelectedChild() {
        for (int i = 0; i < getChildCount(); i++) {
            TrackItemView child = (TrackItemView) getChildAt(i);
            if (child.getClickSelected()) {
                return child;
            }
        }
        return null;
    }

    /**
     * 能否移到新的位置
     *
     * @return
     */
    private boolean isCanMove(TrackItemView child) {
        int left = child.getLeft();
        int row = getRow(child.getTop());
        long leftTime = getDuration() * left / getMeasuredWidth();
        long rightTime = leftTime + child.getBean().getDuration();
        for (TrackMediaBean bean : trackMedias) {
            if (child.getBean() == bean) continue;
            long start = bean.getAtTrackTime();
            long end = start + bean.getDuration();
            if (!(leftTime > end || rightTime < start) && row == bean.getRow()) {//有交界
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
        int row = getRow(child.getTop());
        long startTime = getDuration() * left / getMeasuredWidth();
        child.getBean().setRow(row);
        child.getBean().setAtTrackTime(startTime);
        recordToStack();
        computeTotalDuration();
        requestLayout();

    }

    /**
     * 获取父容器
     *
     * @return
     */
    private TrackContainer getTrackContainer() {
        return (TrackContainer) getTimeRuleContainer().getParent();
    }

    private TimeRuleContainer getTimeRuleContainer() {
        return (TimeRuleContainer) getParent().getParent();
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
