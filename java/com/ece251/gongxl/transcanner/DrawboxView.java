package com.ece251.gongxl.transcanner;

/**
 * Created by hch on 2015/3/1.
 */
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

public class DrawboxView extends ImageView {
    private  Paint mPaint = new Paint();
    private  Paint zPaint = new Paint();
    private  Path mPath = new Path();
    private  float mX, mY;
    private  float left=9999, right=0, bottom=0, top=9999;
    private  static int flag = 0;
    private  Bitmap bitmap = null;
    private  int _width, _height, _LengthFrame;
    private  double _FrameRate;
    private void init(){
        Log.i("Myview", "Init");
        zPaint.setColor(Color.WHITE);
        zPaint.setAntiAlias(true);
        zPaint.setStyle(Paint.Style.STROKE);
        zPaint.setStrokeCap(Paint.Cap.ROUND);
        zPaint.setStrokeWidth(1);

        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);
        flag=0;
        resetBoundary();
    }
    public DrawboxView(Context context) {

        super(context);
        Log.i("Myview", "Init1");
        init();
    }

    public DrawboxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("Myview", "Init2");
        init();
    }

    public DrawboxView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i("Myview", "Init3");
        init();
    }

    public void resetBoundary(){
        left=9999;
        right=0;
        bottom=0;
        top=9999;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        right=x;
        bottom=y;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.v("Himi", "ACTION_DOWN"+String.valueOf(x)+" "+String.valueOf(y));
            //mPath.reset();
            //mPath.moveTo(x, y);
            //resetBoundary();
            left=x;
            top=y;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.v("Himi", "ACTION_UP"+String.valueOf(x)+" "+String.valueOf(y));
            invalidate();
            if(x < left){ right=left; left=x;}
            if(y < top){ bottom=top; top=y; }

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            Log.v("Himi", "ACTION_MOVE"+String.valueOf(x)+" "+String.valueOf(y));
            //mPath.quadTo(mX, mY, (mX + x)/2, (mY + y)/2);
            flag=2;
            invalidate();
        }

        mX=x;
        mY=y;
        return true;
    }

    public void showFrame(Bitmap bt){
        //flag=4;
        Log.v("Myview","showFrame");
        bitmap=bt;
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
        canvas.drawBitmap(bitmap, 0, 0, null);
        if(flag==2){
            canvas.drawRect(left, top, right, bottom, mPaint);
        }




    }
}
