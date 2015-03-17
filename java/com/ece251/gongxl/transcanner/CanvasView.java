package com.ece251.gongxl.transcanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 3/14/15.
 */
public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {
    private final SurfaceHolder surfaceHolder;
    private volatile boolean canvasViewReady = false;
    private int paintColor;
    private float paintWidth;
    private int drawType;
    private List<Drawing> drawList;
    private Drawing curve;
    private Drawing line;
    private BluetoothService bluetoothService;
    private Handler handler;

    public final static int PAINT_STROKE_SMALL = 1;
    public final static int PAINT_STROKE_MEDIUM = 3;
    public final static int PAINT_STROKE_BIG = 5;

    public final static int DRAW_TYPE_LINE = 0;
    public final static int DRAW_TYPE_CURVE = 1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case  MotionEvent.ACTION_DOWN:
                switch (getDrawType()) {
                    case CanvasView.DRAW_TYPE_LINE:
                        startLine(touchX, touchY);
                        break;
                    case CanvasView.DRAW_TYPE_CURVE:
                        startCurve(touchX, touchY);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (getDrawType()) {
                    case CanvasView.DRAW_TYPE_LINE:
                        finishLine(touchX, touchY);
                        break;
                    case CanvasView.DRAW_TYPE_CURVE:
                        finishCurve(touchX, touchY);
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (getDrawType()) {
                    case CanvasView.DRAW_TYPE_LINE:
                        break;
                    case CanvasView.DRAW_TYPE_CURVE:
                        addCurvePoint(touchX, touchY);
                        break;
                }
                break;
        }
        return true;
    }


    public CanvasView(Context context, BluetoothService bluetoothService) {
        super(context);
        this.bluetoothService = bluetoothService;
        this.paintColor = Color.BLACK;
        this.paintWidth = PAINT_STROKE_SMALL;
        this.drawType = DRAW_TYPE_CURVE;
        this.handler = getHandler();
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.curve = null;
        this.line = null;
        this.drawList = new ArrayList<Drawing>();
    }

    public void setPaintColor(int color) {
        this.paintColor = color;
    }

    public void setPaintStroke(int strokeSize) {
        this.paintWidth = strokeSize;
    }

    public void setDrawType(int type) {
        this.drawType = type;
    }

    public int getDrawType() {
        return this.drawType;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        canvasViewReady = true;
        new RenderThread().start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
            canvasViewReady = false;
    }

    public void startLine(float x, float y) {
        line = new Drawing(paintWidth, paintColor);
        line.moveTo(x, y);
    }

    public void finishLine(float x, float y) {
        if(line != null) {
            line.lineTo(x, y);
            synchronized (drawList) {
                drawList.add(line);
            }
            line = null;
        }
    }

    synchronized public void startCurve(float startX, float startY) {
        curve = new Drawing(paintWidth, paintColor);
        curve.moveTo(startX, startY);
    }

    synchronized public void  addCurvePoint(float x, float y) {
        if(curve != null) {
            curve.lineTo(x, y);
        }
    }

    public void finishCurve(float x, float y) {
        synchronized (curve) {
            if(curve != null) {
                curve.lineTo(x, y);
                System.out.println(curve.toString());
                synchronized (drawList) {
                    drawList.add(curve);
                }
            }
        }
        syncDrawing(curve);
    }

    private void syncDrawing(Drawing drawing) {
        System.out.println(drawing.toString());
        bluetoothService.send(drawing.toString() + ":ENDDRAWING");
        this.curve = null;
    }

    class RenderThread extends Thread {
        Canvas canvas = null;
        public void run() {
            super.run();
            while (canvasViewReady) {
                if (!surfaceHolder.getSurface().isValid())
                    return;
                if (!canvasViewReady) {
                    return;
                }
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if(canvas != null)
                        canvas.drawColor(Color.WHITE);
                    if(curve != null) {
                        synchronized (curve) {
                            if(curve == null) continue;
                            Paint paint = new Paint();
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(paintColor);
                            paint.setStrokeWidth(paintWidth);
                            if(canvas != null)
                                canvas.drawPath(curve, paint);
                        }
                    }
                    synchronized (drawList) {
                        for (Drawing drawing : drawList) {
                            Paint paint = new Paint();
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(drawing.color);
                            paint.setStrokeWidth(drawing.width);
                            canvas.drawPath(drawing, paint);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("");
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    public void addDrawing(String drawingMessage) {
        String[] drawingInfo = drawingMessage.split(":");
        for(String part : drawingInfo)
            System.out.println("addDrawing" + part);
        String[] coordinates = drawingInfo[3].trim().split(" ");
        float width = Float.valueOf(drawingInfo[1]);
        int color = Integer.valueOf(drawingInfo[2]);
        int count = 0;
        Drawing drawing = new Drawing(width, color);
        for(String coordinate : coordinates) {
            System.out.println("coordinate" + coordinate);
            String[] cord = coordinate.split(",");
            float x = Float.valueOf(cord[0]);
            float y = Float.valueOf(cord[1]);
            if(count == 0)
                drawing.moveTo(x, y);
            else
                drawing.lineTo(x, y);
            count ++;
        }
        synchronized (drawList) {
            drawList.add(drawing);
        }
    }

    class Point {
        public final float x;
        public final float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + "," + y;
        }
    }

    class Drawing extends Path {
        public final float width;
        public final int color;
        private final List<Point> track;

        @Override
        synchronized public void moveTo(float x, float y) {
            super.moveTo(x, y);
            track.add(new Point(x, y));
        }

        @Override
        synchronized public void lineTo(float x, float y) {
            super.lineTo(x, y);
            track.add(new Point(x, y));

        }

        public Drawing(float width, int color ) {
            super();
            this.width = width;
            this.color = color;
            this.track = new ArrayList<Point>();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("DRAWING:");
            System.out.println("color" + Color.BLACK);
            stringBuilder.append(this.width+ ":" + this.color + ":");
            for(Point point : track)
                stringBuilder.append(point + " ");
            return stringBuilder.toString();
        }
    }
}
