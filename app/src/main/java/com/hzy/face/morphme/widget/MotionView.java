package com.hzy.face.morphme.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MotionView extends View implements View.OnTouchListener {
    private Paint mPointPaint, mLinePaint, mArrowPaint, mPinPaint, mNextPointPaint;

    public MotionView(@NonNull Context context) {
        super(context);
        init();
    }

    public MotionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MotionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private List<List<Point>> mQueueList = new ArrayList<>();

    public List<Point> getFlowQueue() {
        List<Point> flowQueue = new ArrayList<>();
        for (List<Point> pointList : mQueueList) {
            for (Point point : pointList) {
                if (point.nextX != 0 && point.nextY != 0) {
                    flowQueue.add(point);
                }
            }
        }
        return flowQueue;
    }

    private List<Point> mFlowQueue = new ArrayList<>(); // 流动点
    private List<Point> mPinPoints = new ArrayList<>(); // 固定的锚点
    public Point mCurPoint;
    public int mEditMode = 1;

    public float mMaxDistance = 100;  // 判定选中的最大距离

    public List<Point> getPinQueue(int width, int height) {
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(width - 1, 0));
        points.add(new Point(width - 1, height - 1));
        points.add(new Point(0, height - 1));
        points.addAll(mPinPoints);
        return points;
    }

    public void clearPoint() {
        mPinPoints.clear();
        mFlowQueue.clear();
        mQueueList.clear();
        isEditAble();
    }

    public class Point {
        public float x;
        public float y;
        public float nextX;
        public float nextY;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public void init() {
        mPointPaint = new Paint();
        mPointPaint.setColor(Color.rgb(250, 81, 12));
        mPointPaint.setAntiAlias(true);

        mNextPointPaint = new Paint();
        mNextPointPaint.setColor(Color.BLUE);
        mNextPointPaint.setAntiAlias(true);

        mPinPaint = new Paint();
        mPinPaint.setColor(Color.YELLOW);
        mPinPaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setAlpha(90);
        mLinePaint.setStrokeWidth(10);
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{20, 5}, 0));

        mArrowPaint = new Paint();
        mArrowPaint.setColor(Color.WHITE);
        mArrowPaint.setAntiAlias(true);
        mArrowPaint.setAlpha(90);
        mArrowPaint.setStrokeWidth(10);

        invalidate();
        setOnTouchListener(this);
    }


    public boolean isEditAble() {
        return mEditMode != 0;
    }

    public void setEditAble(int editAble) {
        mEditMode = editAble;
        invalidate();
    }

    Path mPinPath = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isEditAble()) {
            for (int i = 0; i < mQueueList.size(); i++) {
                List<Point> pointList = mQueueList.get(i);
                for (int j = 0; j < pointList.size(); j++) {
                    Point point = pointList.get(j);
                    if (j > 0) {
                        drawLineArrows(canvas, pointList.get(j - 1), point);
                    }
                    if (mCurPoint != null && (j == pointList.size() - 1) && (i == mQueueList.size() - 1)) {
                        drawLineArrows(canvas, point, mCurPoint);
                    }
                }
            }

            for (int i = 0; i < mQueueList.size(); i++) {
                List<Point> pointList = mQueueList.get(i);
                if (pointList.size() <= 1) {
                    continue;
                }
                for (int j = 0; j < pointList.size(); j++) {
                    Point point = pointList.get(j);
                    canvas.drawCircle(point.x, point.y, 10, mPointPaint);

                    canvas.drawCircle(point.nextX, point.nextY, 10, mNextPointPaint);

                    if (mCurPoint != null && (j == pointList.size() - 1) && (i == mQueueList.size() - 1)) {
                        canvas.drawCircle(mCurPoint.x, mCurPoint.y, 10, mPointPaint);
                    }
                }
            }

            // 画锚点
            for (Point point : mPinPoints) {
                canvas.drawCircle(point.x, point.y, 10, mPinPaint);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isEditAble()) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point point = new Point(event.getX(), event.getY());
            if (mEditMode == 1) {
                mFlowQueue = new ArrayList<>();
                mFlowQueue.add(point);
                mQueueList.add(mFlowQueue);
            } else {
                mPinPoints.add(point);
            }
            invalidate();
            return mEditMode == 1;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mCurPoint = new Point(event.getX(), event.getY());
            if (mEditMode == 1) {
                Point startPoint = mFlowQueue.get(mFlowQueue.size() - 1);
                if (getDistance(mCurPoint, startPoint) >= mMaxDistance) {
                    startPoint.nextX = startPoint.x + (mCurPoint.x - startPoint.x) / 4;
                    startPoint.nextY = startPoint.y + (mCurPoint.y - startPoint.y) / 4;
                    mFlowQueue.add(mCurPoint);
                }
            }
            invalidate();
        } else {
            mCurPoint = null;
        }
        return false;
    }

    public double getDistance(Point A, Point B) {
        return Math.sqrt(Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2));
    }

    public void drawLineArrows(Canvas canvas, Point pointA, Point pointB) {
        float x1 = pointA.x;
        float y1 = pointA.y;
        float x2 = pointB.x;
        float y2 = pointB.y;
        float arrowSize = 40;

        // 画直线
        canvas.drawLine(x1, y1, x2, y2, mLinePaint);

        if (pointB == mCurPoint && getDistance(mCurPoint, mFlowQueue.get(mFlowQueue.size() - 1)) < mMaxDistance / 4) {
            return;
        }

        // 箭头中的第一条线的起点
        int x3 = 0;
        int y3 = 0;

        // 箭头中的第二条线的起点
        int x4 = 0;
        int y4 = 0;

        double awrad = Math.atan(3.5 / 8);
        double[] arrXY_1 = rotateVec(x2 - x1, y2 - y1, awrad, arrowSize);
        double[] arrXY_2 = rotateVec(x2 - x1, y2 - y1, -awrad, arrowSize);

        // 第一端点
        double X3 = x2 - arrXY_1[0];
        x3 = (int) X3;
        double Y3 = y2 - arrXY_1[1];
        y3 = (int) Y3;

        // 第二端点
        double X4 = x2 - arrXY_2[0];
        x4 = (int) X4;
        double Y4 = y2 - arrXY_2[1];
        y4 = (int) Y4;

        canvas.drawLine(x3, y3, x2, y2, mArrowPaint);
        canvas.drawLine(x4, y4, x2, y2, mArrowPaint);
    }

    private double[] rotateVec(float px, float py, double ang, double arrowSize) {
        double[] math = new double[2];
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        double d = Math.sqrt(vx * vx + vy * vy);
        vx = vx / d * arrowSize;
        vy = vy / d * arrowSize;
        math[0] = vx;
        math[1] = vy;
        return math;
    }
}
