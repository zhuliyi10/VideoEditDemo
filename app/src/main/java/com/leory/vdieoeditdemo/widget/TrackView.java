package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.leory.vdieoeditdemo.bean.TrackMediaBean;
import com.leory.vdieoeditdemo.utils.ScreenUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 轨道视频
 * @Author: leory
 * @Time: 2020/11/4
 */
public class TrackView extends View {
    private static final String TAG = TrackView.class.getSimpleName();
    private float scaleFactor = 1f;//缩放倍数
    private float lastScaleFactor = 1f;//上一次倍数

    //时间刻度尺相关
    float timeLineHeight;//时间刻度尺高度
    Rect timeBounds = new Rect();
    ;//mm:ss字体大小
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
    long defaultUsPerUnit = 1000 * 1000;//默认一个单元多少us
    long unitUs = defaultUsPerUnit;//一个单元多少us
    float pxPerUs;

    private List<List<TrackMediaBean>> medias = new ArrayList<>();
    private DateFormat df = new SimpleDateFormat("mm:ss");

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
        setBackgroundColor(Color.DKGRAY);
        timeLineHeight = dp2px(20);
        timePaint.setStrokeWidth(3f);
        timePaint.setColor(Color.GRAY);
        timePaint.setTextSize(sp2px(10f));

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
        pxPerUs=unitSize/defaultUsPerUnit;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (medias.size() > 0) {
            long duration = medias.get(0).get(0).getDuration();
            width = (int) (duration * 1f / defaultUsPerUnit * maxUnitSize * scaleFactor);
        }

        setMeasuredDimension(width, height);


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTimeRuler(canvas);
    }

    private void drawTimeRuler(Canvas canvas) {
        if (medias.size() > 0) {
            long unitCount = Math.round(getMeasuredWidth() / unitSize);//单元格数量
            Log.d(TAG, "unitCount: " + unitCount);
            for (int i = 0; i <= unitCount; i++) {
                if (i % 2 == 0) {//绘制mm:ss

                    String time = "00:00";
                    if (scaleFactor <= 2) {
                        time = df.format(new Date(Math.round(unitUs* i / 1000) ));
                    } else {
                        if (i % unitUs == 0) {//整秒
                            time = df.format(new Date(i / unitUs * defaultUsPerUnit / 1000));
                        } else {
                            long mod = i % unitUs;
                            if (unitUs == 4) {
                                time = mod / 2 * 15 + "f";
                            } else if (unitUs == 6) {
                                time = mod / 2 * 10 + "f";
                            } else if (unitUs == 12) {
                                time = mod / 2 * 5 + "f";
                            }
                        }
                    }
                    timePaint.getTextBounds(time, 0, time.length(), timeBounds);
                    float x = unitSize * i - timeBounds.width() / 2f;
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


    public void addTrack(TrackMediaBean bean) {
        List<TrackMediaBean> data = new ArrayList<>();
        data.add(bean);
        medias.add(data);
        requestLayout();
    }

    /**
     * 设置缩放倍数
     *
     * @param scaleFactor
     */
    public void setScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        pxPerUs*=scaleFactor;
        if (scaleFactor != lastScaleFactor) {
            float factor = scaleFactor / lastScaleFactor;
            unitSize *= factor;
            HorizontalScrollView parent= (HorizontalScrollView) getParent();
            Log.d(TAG, "setScale: "+parent.getScrollX());
            if (medias.size() > 0) {
                int scroll= (int) (parent.getScrollX()*(factor-1));
                parent.setScrollX(parent.getScrollX()+scroll);
            }
            measureUnitSize();
            requestLayout();
        }

    }

    private void measureUnitSize() {

        if (lastScaleFactor <= 12 && lastScaleFactor > 6) {//1/12秒为单位
            if (scaleFactor > 12) {
                unitSize = minUnitSize*(scaleFactor/12);
            } else if (scaleFactor <= 6) {
                unitSize = unitOneSixSecond*(scaleFactor/6);
                unitUs = 6;
            }
        } else if (lastScaleFactor <= 6 && lastScaleFactor > 3) {//1/6秒为单位
            if (scaleFactor > 6) {
                unitSize = minUnitSize*(scaleFactor/6);
                unitUs = 12;
            } else if (scaleFactor <= 3) {
                unitSize = unitQuartSecond*(scaleFactor/3);
                unitUs = 4;
            }
        } else if (lastScaleFactor <= 3 && lastScaleFactor > 2) {//1/4秒为单位
            if (scaleFactor > 3) {
                unitSize = minUnitSize*(scaleFactor/3);
                unitUs = 6;
            } else if (scaleFactor <= 2) {
                unitSize = unitHalfSecond*(scaleFactor/2);
                unitUs = defaultUsPerUnit / 2;
            }
        } else if (lastScaleFactor <= 2 && lastScaleFactor > 1) {//1/2秒为单位
            if (scaleFactor > 2) {
                unitSize = minUnitSize*(scaleFactor/2);
                unitUs = 4;
            } else if (scaleFactor <= 1) {
                unitSize = unitSecond*(scaleFactor/1);
                unitUs = defaultUsPerUnit;
            }
        } else if (lastScaleFactor <= 1 && lastScaleFactor > 1f / 2) {//1秒为单位
            if (scaleFactor > 1) {
                unitSize = minUnitSize*(scaleFactor/1);
                unitUs = defaultUsPerUnit / 2;
            } else if (scaleFactor <= 1f / 2) {
                unitSize = unitOneAndHalfSecond*(scaleFactor/(1f / 2));
                unitUs = defaultUsPerUnit * 3 / 2;
            }
        } else if (lastScaleFactor <= 1f / 2 && lastScaleFactor > 1f / 3) {//1.5秒为单位
            if (scaleFactor > 1f / 2) {
                unitSize = minUnitSize*(scaleFactor/(1f / 2));
                unitUs = defaultUsPerUnit;
            } else if (scaleFactor <= 1f / 3) {
                unitSize = unitTwoAndHalfSecond*(scaleFactor/(1f / 3));
                unitUs = defaultUsPerUnit * 5 / 2;
            }
        } else if (lastScaleFactor <= 1f / 3 && lastScaleFactor > 1f / 5) {//2.5秒为单位
            if (scaleFactor > 1f / 3) {
                unitSize = minUnitSize*(scaleFactor/(1f / 3));
                unitUs = defaultUsPerUnit * 3 / 2;
            } else if (scaleFactor <= 1f / 5) {
                unitSize = unitFiveSecond*(scaleFactor/(1f / 5));
                unitUs = defaultUsPerUnit * 5;
            }
        } else if (lastScaleFactor <= 1f / 5 && lastScaleFactor > 1f / 10) {//5秒为单位
            if (scaleFactor > 1f / 5) {
                unitSize = minUnitSize*(scaleFactor/(1f / 5));
                unitUs = defaultUsPerUnit * 5 / 2;
            } else if (scaleFactor <= 1f / 10) {
                unitSize = unitTenSecond*(scaleFactor/(1f / 10));
                unitUs = defaultUsPerUnit * 10;
            }
        } else if (lastScaleFactor <= 1f / 10 && lastScaleFactor > 1f / 20) {//10秒为单位
            if (scaleFactor > 1f / 10) {
                unitSize = minUnitSize*(scaleFactor/(1f / 10));;
                unitUs = defaultUsPerUnit * 5;
            } else if (scaleFactor <= 1f / 20) {
                unitSize = unitTwentySecond*(scaleFactor/(1f / 20));;
                unitUs = defaultUsPerUnit * 20;
            }
        } else if (lastScaleFactor <= 1f / 20) {//20秒为单位
            unitUs = defaultUsPerUnit * 20;
            unitSize = unitTwentySecond;
        }
        Log.d(TAG, "scaleFactor: " + scaleFactor);
        Log.d(TAG, "unitSize: " + unitSize);
        Log.d(TAG, "unitUs: " + unitUs);
        lastScaleFactor = scaleFactor;

    }

    private int dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }

    private float sp2px(float dp) {
        return ScreenUtils.sp2px(getContext(), dp);
    }
}
