package com.leory.vdieoeditdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.leory.vdieoeditdemo.utils.ScreenUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 时间刻盘
 * @Author: leory
 * @Time: 2020/11/4
 */
public class TimeLineView extends View {
    private static final String TAG=TimeLineView.class.getSimpleName();
    private Paint paint;
    private long durationInUs = 0L;//时间us
    int textWidth = 0, textHeight = 0;//字体宽度，字体高度
    float maxUnitSize;//一个单元最大宽度
    float minUnitSize;// 一个单元最小宽度
    float unitSize;//一个单元的宽度，定义为:.中心到mm:ss中心距离
    long unitCount = 0L;//单元格的数量
    float dotSize = 3f;
    float scaleFactor = 1f;//缩放倍数
    long defaultUsPerUnit = 1000 * 1000;//默认一个单元多少us
    float pxPerUs;//1us包含多少像素
    private DateFormat df = new SimpleDateFormat("mm:ss");

    public TimeLineView(Context context) {
        super(context);
        init(context);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.GRAY);
        paint.setTextSize(sp2px(10f));
        maxUnitSize = ScreenUtils.getScreenWidth(context) / 8f;
        minUnitSize = maxUnitSize / 2;
        unitSize = maxUnitSize;
        pxPerUs =  unitSize/defaultUsPerUnit;
        Rect bounds = new Rect();
        paint.getTextBounds("00:00", 0, 5, bounds);
        textWidth = bounds.width();
        textHeight = bounds.height();
        dotSize = dp2px(2f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, textHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i <= unitCount; i++) {
            if (i % 2 == 0) {//绘制mm:ss
                float x = getPaddingLeft() + unitSize * i - textWidth / 2f;
                float y = (getMeasuredHeight() + textHeight) / 2f;
                int timeScale=(int)(1/scaleFactor);
                long us =  defaultUsPerUnit*timeScale;
                String time = df.format(new Date(us*i / 1000));
                canvas.drawText(time, x, y, paint);
            } else {//绘制.
                canvas.drawCircle(getPaddingLeft() + unitSize * i
                        , getMeasuredHeight() / 2f
                        , dotSize / 2f
                        , paint);
            }
        }
    }


    /**
     * 设置时长
     * @param us
     */
    public void setDuration(long us) {
        if (us > 0 && this.durationInUs != us) {
            this.durationInUs = us;
            pxPerUs = pxPerUs * scaleFactor;
            float width= pxPerUs*durationInUs;//视频实际占用宽度
            unitCount= (long) (width/unitSize);
        }
    }

    /**
     * 设置缩放倍数
     *
     * @param scaleFactor
     */
    public void setScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        unitSize = scaleFactor * unitSize;
        if (unitSize > maxUnitSize) {
            unitCount *= 2;
            unitSize /= 2;
        }
        if (unitSize < minUnitSize) {
            unitCount /= 2;
            unitSize *= 2;
        }
        Log.d(TAG, "unitCount: "+unitCount);
        Log.d(TAG, "unitSize: "+unitSize);
    }

    private float sp2px(float dp) {
        return ScreenUtils.sp2px(getContext(), dp);
    }

    private float dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }
}
