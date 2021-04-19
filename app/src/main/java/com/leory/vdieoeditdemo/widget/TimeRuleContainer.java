package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leory.vdieoeditdemo.bean.TrackMediaBean;
import com.leory.vdieoeditdemo.utils.ScreenUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 时间刻度表容器, 处理缩放系数
 * @Author: leory
 * @Time: 2020/11/10
 */
public class TimeRuleContainer extends ViewGroup {
    private static final String TAG = TimeRuleContainer.class.getSimpleName();
    private float scaleFactor = 1f;//缩放倍数
    private float lastScaleFactor = 1f;//上一次倍数
    float tempFactor = 1f;
    //时间刻度尺相关
    float timeLineHeight;//时间刻度尺高度
    Rect timeBounds = new Rect();
    Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//时间画笔
    float dotSize = 0f;
    float unitSize;//一个单元的宽度，定义为:.中心到mm:ss中心距离
    float maxUnitSize;//一个单元最大宽度
    float minUnitSize;// 一个单元最小宽度
    float unitOneTwelfthSixSecond;//1/12秒为单位的最大宽度
    float unitOneSixSecond;//1/6秒为单位的最大宽度
    float unitQuartSecond;//1/4秒为单位的最大宽度
    float unitHalfSecond;//1/2秒为单位的最大宽度
    float unitSecond;//1秒为单位的最大宽度
    float unitOneAndHalfSecond;//1.5秒为单位的最大宽度
    float unitTwoAndHalfSecond;//2.5秒为单位的最大宽度
    float unitFiveSecond;//5秒为单位的最大宽度
    float unitTenSecond;//10秒为单位的最大宽度
    float unitTwentySecond;//20秒为单位的最大宽度
    long defaultMsPerUnit = 1000;//默认一个单元多少ms
    long unitMs = defaultMsPerUnit;//一个单元多少ms
    float pxPerMs;
    private long duration = 0L;//轨道总时长
    private long videoDuration = 0L;//视频总时长
    private TrackMediaBean trackMediaBean;
    int videoTrackHeight;//视频轨道的高度
    int videoPaddingTop;//视频轨道默认paddingTop
    private TrackView trackView;
    private TextView trackVideo;
    private ScrollView verticalScroll;//用于轨道垂直滚动

    private long currentTime;
    boolean isScaling = false;//是否在缩放

    private DateFormat df = new SimpleDateFormat("mm:ss");

    public TimeRuleContainer(Context context) {
        super(context);
        setClipToPadding(false);
        setClipChildren(false);
        setBackgroundColor(Color.BLACK);
        timeLineHeight = dp2px(20);
        timePaint.setStrokeWidth(3f);
        timePaint.setColor(Color.GRAY);
        timePaint.setTextSize(sp2px(10f));
        timePaint.setTextAlign(Paint.Align.CENTER);
        videoTrackHeight = dp2px(60);
        videoPaddingTop = dp2px(10);
        dotSize = dp2px(2f);
        maxUnitSize = ScreenUtils.getScreenWidth(context) / 8f;//初始状态一秒代表一个单元格,占屏幕1/8
        unitOneTwelfthSixSecond = maxUnitSize;
        unitOneSixSecond = maxUnitSize;
        unitQuartSecond = maxUnitSize * 3 / 4;
        unitHalfSecond = maxUnitSize;
        unitSecond = maxUnitSize;
        minUnitSize = maxUnitSize / 2;
        unitOneAndHalfSecond = maxUnitSize * 3 / 4;
        unitTwoAndHalfSecond = maxUnitSize * 5 / 6;
        unitFiveSecond = maxUnitSize;
        unitTenSecond = maxUnitSize;
        unitTwentySecond = maxUnitSize;
        unitSize = unitSecond;
        pxPerMs = unitSize / defaultMsPerUnit;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int totalWidth = (int) (duration * maxUnitSize * scaleFactor / defaultMsPerUnit);
        if (trackVideo != null) {
            int specSize = (int) (totalWidth * trackMediaBean.getDuration() / duration);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(videoTrackHeight, MeasureSpec.EXACTLY);
            trackVideo.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
        if (verticalScroll != null) {
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY);
            int childHeight = (int) (height - timeLineHeight - videoPaddingTop - videoTrackHeight);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            verticalScroll.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
        setMeasuredDimension(totalWidth, height);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = (int) timeLineHeight;
        int width = getMeasuredWidth();
        if (trackVideo != null) {
            int l = (int) (width * trackMediaBean.getAtTrackTime() / duration);
            height += videoPaddingTop;
            trackVideo.layout(l, height, l + trackVideo.getMeasuredWidth(), height + videoTrackHeight);
        }
        if (verticalScroll != null) {
            verticalScroll.layout(0, getMeasuredHeight() - verticalScroll.getMeasuredHeight(), verticalScroll.getMeasuredWidth(), getMeasuredHeight());
        }
        if (isScaling) {//为了调整缩放后当前时间不变
            Log.d(TAG, "onMeasure: getCurrentTime="+getCurrentTime());
            Log.d(TAG, "onMeasure: getDuration="+getDuration());
            float scroll = 1f * getMeasuredWidth() * getCurrentTime() / getDuration();
            Log.d(TAG, "onMeasure: scroll="+scroll);
            Log.d(TAG, "onMeasure: totalWidth="+getMeasuredWidth());
            getTrackContainer().setScrollX((int) scroll);
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawTimeRuler(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }


    /**
     * 获取轨道trackView
     *
     * @return
     */
    public TrackView getTrackView() {
        return trackView;
    }

    /**
     * 添加一个轨道
     *
     * @param bean
     */
    public void addTrack(TrackMediaBean bean) {
        if (duration == 0) {//第一个是视频
            duration = bean.getDuration();
            videoDuration = duration;
            this.trackMediaBean = bean;
            trackVideo = new TextView(getContext());
            trackVideo.setText("视频轨道");
            trackVideo.setTextColor(Color.WHITE);
            trackVideo.setBackgroundColor(Color.BLUE);
            addView(trackVideo);
            trackView = new TrackView(getContext());
            verticalScroll = new ScrollView(getContext());
            verticalScroll.setClipChildren(false);
            verticalScroll.setClipToPadding(false);
            verticalScroll.addView(trackView, makeLayoutParams());
            addView(verticalScroll, 0, makeLayoutParams());
        } else {//添加音频数据
            //计算插入的时间点 开始
            bean.setAtTrackTime(duration * getTrackContainer().getScrollX() / getMeasuredWidth());
            if (bean.getAtTrackTime() + bean.getDuration() > duration) {
                duration = bean.getAtTrackTime() + bean.getDuration();
            }
            Log.d(TAG, "setAtTrackTime: " + bean.getAtTrackTime());
            //计算插入的时间点 结束
            trackView.addTrack(bean);
        }
        requestLayout();
    }

    /**
     * 设置缩放倍数
     *
     * @param scaleFactor
     */
    public void setScale(float scaleFactor) {
        if (scaleFactor != lastScaleFactor) {

            this.scaleFactor = scaleFactor;
            pxPerMs *= scaleFactor;
            tempFactor = scaleFactor / lastScaleFactor;
            unitSize *= tempFactor;
            measureUnitSize();
            requestLayout();

        }
    }

    public void startScale(boolean isScale) {
        Log.d(TAG, "startScale: "+isScale);
        this.isScaling = isScale;
    }

    /**
     * 设置当前的时间
     */
    public void setCurrentTime(){
        currentTime=getDuration()*getTrackContainer().getScrollX()/getMeasuredWidth();
        Log.d(TAG, "setCurrentTime: "+currentTime);
    }

    /**
     * 获取当前时间
     */
    public long getCurrentTime(){
        return currentTime;
    }

    /**
     * 获取总时长
     *
     * @return
     */
    protected long getDuration() {
        return duration;
    }

    /**
     * 设置时长
     *
     * @param duration
     */
    protected void setDuration(long duration) {
        if (duration > videoDuration) {//不能短于视频时长
            this.duration = duration;
        } else {
            this.duration = videoDuration;
        }

    }


    /**
     * 画时间刻度尺
     *
     * @param canvas
     */
    private void drawTimeRuler(Canvas canvas) {
        timePaint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, getMeasuredWidth(), timeLineHeight + videoPaddingTop, timePaint);
        timePaint.setColor(Color.GRAY);
        if (duration > 0) {
            long unitCount = Math.round(getMeasuredWidth() / unitSize);//单元格数量
            Log.d(TAG, "unitCount: " + unitCount);
            for (int i = 0; i <= unitCount; i++) {
                if (i % 2 == 0) {//绘制mm:ss

                    String time = "00:00";
                    if (scaleFactor <= 2) {
                        time = df.format(new Date(Math.round(unitMs * i)));
                    } else {
                        if (i % unitMs == 0) {//整秒
                            time = df.format(new Date(i / unitMs * defaultMsPerUnit));
                        } else {
                            long mod = i % unitMs;
                            if (unitMs == 4) {
                                time = mod / 2 * 15 + "f";
                            } else if (unitMs == 6) {
                                time = mod / 2 * 10 + "f";
                            } else if (unitMs == 12) {
                                time = mod / 2 * 5 + "f";
                            }
                        }
                    }
                    timePaint.getTextBounds(time, 0, time.length(), timeBounds);
                    float x = unitSize * i;
                    float y = (timeLineHeight + timeBounds.height()) / 2f;
                    canvas.drawText(time, x, y, timePaint);
                } else {//绘制.
                    canvas.drawCircle(unitSize * i
                            , timeLineHeight / 2f
                            , dotSize / 2f
                            , timePaint);
                }
            }
        }
    }

    /**
     * 计算1个单元格的像素数
     */
    private void measureUnitSize() {

        if (lastScaleFactor <= 12 && lastScaleFactor > 6) {//1/12秒为单位
            if (scaleFactor > 12) {
                unitSize = minUnitSize * (scaleFactor / 12);
            } else if (scaleFactor <= 6) {
                unitSize = unitOneSixSecond * (scaleFactor / 6);
                unitMs = 6;
            }
        } else if (lastScaleFactor <= 6 && lastScaleFactor > 3) {//1/6秒为单位
            if (scaleFactor > 6) {
                unitSize = minUnitSize * (scaleFactor / 6);
                unitMs = 12;
            } else if (scaleFactor <= 3) {
                unitSize = unitQuartSecond * (scaleFactor / 3);
                unitMs = 4;
            }
        } else if (lastScaleFactor <= 3 && lastScaleFactor > 2) {//1/4秒为单位
            if (scaleFactor > 3) {
                unitSize = minUnitSize * (scaleFactor / 3);
                unitMs = 6;
            } else if (scaleFactor <= 2) {
                unitSize = unitHalfSecond * (scaleFactor / 2);
                unitMs = defaultMsPerUnit / 2;
            }
        } else if (lastScaleFactor <= 2 && lastScaleFactor > 1) {//1/2秒为单位
            if (scaleFactor > 2) {
                unitSize = minUnitSize * (scaleFactor / 2);
                unitMs = 4;
            } else if (scaleFactor <= 1) {
                unitSize = unitSecond * (scaleFactor / 1);
                unitMs = defaultMsPerUnit;
            }
        } else if (lastScaleFactor <= 1 && lastScaleFactor > 1f / 2) {//1秒为单位
            if (scaleFactor > 1) {
                unitSize = minUnitSize * (scaleFactor / 1);
                unitMs = defaultMsPerUnit / 2;
            } else if (scaleFactor <= 1f / 2) {
                unitSize = unitOneAndHalfSecond * (scaleFactor / (1f / 2));
                unitMs = defaultMsPerUnit * 3 / 2;
            }
        } else if (lastScaleFactor <= 1f / 2 && lastScaleFactor > 1f / 3) {//1.5秒为单位
            if (scaleFactor > 1f / 2) {
                unitSize = minUnitSize * (scaleFactor / (1f / 2));
                unitMs = defaultMsPerUnit;
            } else if (scaleFactor <= 1f / 3) {
                unitSize = unitTwoAndHalfSecond * (scaleFactor / (1f / 3));
                unitMs = defaultMsPerUnit * 5 / 2;
            }
        } else if (lastScaleFactor <= 1f / 3 && lastScaleFactor > 1f / 5) {//2.5秒为单位
            if (scaleFactor > 1f / 3) {
                unitSize = minUnitSize * (scaleFactor / (1f / 3));
                unitMs = defaultMsPerUnit * 3 / 2;
            } else if (scaleFactor <= 1f / 5) {
                unitSize = unitFiveSecond * (scaleFactor / (1f / 5));
                unitMs = defaultMsPerUnit * 5;
            }
        } else if (lastScaleFactor <= 1f / 5 && lastScaleFactor > 1f / 10) {//5秒为单位
            if (scaleFactor > 1f / 5) {
                unitSize = minUnitSize * (scaleFactor / (1f / 5));
                unitMs = defaultMsPerUnit * 5 / 2;
            } else if (scaleFactor <= 1f / 10) {
                unitSize = unitTenSecond * (scaleFactor / (1f / 10));
                unitMs = defaultMsPerUnit * 10;
            }
        } else if (lastScaleFactor <= 1f / 10 && lastScaleFactor > 1f / 20) {//10秒为单位
            if (scaleFactor > 1f / 10) {
                unitSize = minUnitSize * (scaleFactor / (1f / 10));
                unitMs = defaultMsPerUnit * 5;
            } else if (scaleFactor <= 1f / 20) {
                unitSize = unitTwentySecond * (scaleFactor / (1f / 20));
                unitMs = defaultMsPerUnit * 20;
            }
        } else if (lastScaleFactor <= 1f / 20) {//20秒为单位
            unitMs = defaultMsPerUnit * 20;
            unitSize = unitTwentySecond;
        }
        Log.d(TAG, "scaleFactor: " + scaleFactor);
        Log.d(TAG, "unitSize: " + unitSize);
        Log.d(TAG, "unitMs: " + unitMs);
        lastScaleFactor = scaleFactor;

    }

    /**
     * 获取父容器
     *
     * @return
     */
    private TrackContainer getTrackContainer() {
        return (TrackContainer) getParent();
    }

    private LayoutParams makeLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private int dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }

    private float sp2px(float dp) {
        return ScreenUtils.sp2px(getContext(), dp);
    }


}
