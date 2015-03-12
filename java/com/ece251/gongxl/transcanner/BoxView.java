package com.ece251.gongxl.transcanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by hch on 2015/3/3.
 */
public class BoxView extends ImageView {
    private Paint mPaint = new Paint();
    private  Paint zPaint = new Paint();
    private Paint mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint dpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mPath = new Path();
    private  float mX, mY;
    private  float left=20, right=200, bottom=200, top=20;
    private  static int flag = 0;
    private Bitmap bitmap = null;
    private  int _width, _height, _LengthFrame;
    private  double _FrameRate;
    private String str = " ";
    private void init(){
        Log.i("Myview", "Init");
        zPaint.setColor(Color.WHITE);
        zPaint.setAntiAlias(true);
        zPaint.setStyle(Paint.Style.STROKE);
        zPaint.setStrokeCap(Paint.Cap.ROUND);
        zPaint.setStrokeWidth(1);

        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(30);
    /*    mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(1);*/

        mAreaPaint.setColor(Color.GRAY);
        mAreaPaint.setStyle(Paint.Style.FILL);
        mAreaPaint.setAlpha(100);

        dpPaint.setColor(Color.WHITE);
        dpPaint.setStyle(Paint.Style.FILL);
        dpPaint.setAlpha(200);
    }
    public BoxView(Context context) {

        super(context);
        Log.i("Myview", "Init1");
        init();
    }

    public BoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("Myview", "Init2");
        init();
    }

    public BoxView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i("Myview", "Init3");
        init();
    }
    public void setPos(int _left, int _right, int _top, int _bottom){
        left = _left;
        right = _right;
        top = _top;
        bottom = _bottom;

    }

    public void displayText(String _str){
        str = _str;
        invalidate();
    }
    public float getLeftPos(){
        return left;
    }
    public float getRightPos(){
        return right;
    }
    public float getTopPos(){
        return top;
    }
    public float getBottomPos(){
        return bottom;
    }

    protected void onDraw(Canvas canvas) {
        /**清空画布**/
            //canvas.drawRect(left, top, right, bottom, mPaint);
            canvas.drawRect(0,0,(float)(right/0.8),top,mAreaPaint);
            canvas.drawRect(0,top,left,(float)(bottom/0.25),mAreaPaint);
            canvas.drawRect(right,top,(float)(right/0.8),(float)(bottom/0.25),mAreaPaint);
            canvas.drawRect(left,bottom,right,(float)(bottom/0.25),mAreaPaint);

            canvas.drawRect(left, (float)(bottom*1.1), right, (float)(bottom*1.5), dpPaint);
            canvas.drawText(str,(float)(left*1.1),(float)(bottom*1.2),mPaint);
    }
}
