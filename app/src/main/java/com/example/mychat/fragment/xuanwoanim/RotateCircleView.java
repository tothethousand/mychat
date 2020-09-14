package com.example.mychat.fragment.xuanwoanim;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//import android.support.annotation.Nullable;


/*
 *@创建者       L_jp
 *@创建时间     2018/10/24 14:59.
 *@描述
 *
 *@更新者         $Author$
 *@更新时间         $Date$
 *@更新描述
 */
public class RotateCircleView extends View {
    private Context mContext;
    private int mScreenHeight;
    private int mCircleCount;//圆的数量
    private int mScreenWidth;
    private Paint mPaint;
    private int defaultRadius = 100;//默认的半径 最开始的一个圆的半径
    private int circleSpace = 10;//每个圆的间隔
    private RectF mRectF;
    private boolean isStop = true;
    private double mSqrt;

    public RotateCircleView(Context context) {
        this(context, null);
    }

    public RotateCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }


    private void init() {
        defaultRadius = ScreenUtil.dip2px(mContext, 100);
//        c²=a²+b²
        if (mPaint == null) {

            mScreenHeight = ScreenUtil.getScreenHeight(mContext);
            mScreenWidth = ScreenUtil.getScreenWidth(mContext);
            int cf = mScreenWidth * mScreenWidth + mScreenHeight * mScreenHeight;
            mSqrt = Math.sqrt(cf);
            mCircleCount = (int) Math.abs((mSqrt - defaultRadius * 2) / (circleSpace * 2));


            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.parseColor("#D2D2D2"));
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2);
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        defaultRadius = ScreenUtil.dip2px(mContext, 100);
        circleSpace = 10;
        try {


            if (acrList == null || acrList.size() <= 0) {
                saveArc();
                invalidate();
                return;
            }
            for (int i = 0; i < acrList.size(); i++) {
                defaultRadius = defaultRadius + circleSpace + i * 5;
                if (defaultRadius >= mSqrt) {
                    break;
                }
                mPaint.setStrokeWidth(2);
                mPaint.setColor(Color.parseColor("#D2D2D2"));
                canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2, defaultRadius, mPaint);

                mRectF = new RectF(mScreenWidth / 2 - defaultRadius, mScreenHeight / 2 - defaultRadius, mScreenWidth / 2 + defaultRadius, mScreenHeight / 2 + defaultRadius);
                mPaint.setColor(Color.parseColor("#FFCC33"));
                mPaint.setStrokeWidth(4);
                AcrBean acrBean = acrList.get(i);
                int start = acrBean.getStart();
                int start2 = acrBean.getStart2();

                canvas.drawArc(mRectF, start, acrBean.getEnd(), false, mPaint);
                canvas.drawArc(mRectF, start2, acrBean.getEnd2(), false, mPaint);
                if (start >= 360) {
                    start = 0;
                }
                acrBean.setStart(start + 1);
                if (start2 >= 360) {
                    start2 = 0;
                }
                acrBean.setStart2(start2 + 1);

            }


            if (!isStop) {
                invalidate();
            }

        } catch (Exception e) {

        }
    }


    private void saveArc() {
        for (int i = 0; i < mCircleCount; i++) {
            Random random = new Random();
            int start = random.nextInt(360);
            int end = 5 + (int) (Math.random() * 15);
            int end2 = 5 + (int) (Math.random() * 15);

            acrList.add(new AcrBean(start, end, start + 180, end2));
        }
    }

    List<AcrBean> acrList = new ArrayList<>();


    public void stopAnim() {
        isStop = true;
        invalidate();
    }

    public void startAnim() {
        isStop = false;
        invalidate();
    }

    class AcrBean {
        private int start;
        private int end;
        private int start2;
        private int end2;

        public AcrBean(int start, int end, int start2, int end2) {
            this.start = start;
            this.end = end;
            this.start2 = start2;
            this.end2 = end2;
        }

        public int getStart2() {
            return start2;
        }

        public void setStart2(int start2) {
            this.start2 = start2;
        }

        public int getEnd2() {
            return end2;
        }

        public void setEnd2(int end2) {
            this.end2 = end2;
        }


        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }


    public static int dip2px(Context ctx, float dpValue) {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
