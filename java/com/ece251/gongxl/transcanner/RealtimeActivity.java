package com.ece251.gongxl.transcanner;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.googlecode.tesseract.android.TessBaseAPI;


public class RealtimeActivity extends Activity {

    private Camera mCamera;
    public static int screenWidth;
    public static int screenHeight;
    private CameraPreview mPreview;
    //private TextView textView;
    public static Bitmap image;
    public static int num = 0;
    public static int mode = 0;
    public Paint mPaint = new Paint();
    private int bleft, bright, btop, bbottom;
    private int frameCount;
    private Handler handler;
    private BoxView boxView;
    private Button button_back;

    private FrameLayout preview;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";

    public static final String lang = "eng";
    private static final String TAG = "RealtimeActivity.java";
    public TessBaseAPI baseApi;

    private void initializeOCR() {
        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();
                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.realtimeactivity);

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();      // 屏幕高（像素，如：800p）
        bleft = (int) (screenWidth * 0.2);
        bright = (int) (screenWidth * 0.8);
        btop = (int) (screenHeight * 0.2);
        bbottom = (int) (screenHeight * 0.25); /////
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                boxView.displayText((String)msg.obj);
            }
        };
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);
        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        boxView = (BoxView) findViewById(R.id.boxview);
        boxView.setPos(bleft, bright, btop, bbottom);
        preview.addView(mPreview);

        initializeOCR();
        frameCount = 0;

        button_back = (Button) findViewById(R.id.button_back);
        button_back.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mode == 0) {
                        num = 0;
                        baseApi = new TessBaseAPI();
                        baseApi.setDebug(true);
                        baseApi.init(DATA_PATH, lang);
                        //mCamera.autoFocus(myAutoFocusCallback);
                        mCamera.setPreviewCallback(
                            new Camera.PreviewCallback() {
                                public void onPreviewFrame(byte[] data, Camera camera) {
                                    if(frameCount ++ != 50) {
                                        return;
                                    }
                                    frameCount = 0;
                                    Camera.Parameters parameters;
                                    parameters = camera.getParameters();
                                    int width = parameters.getPreviewSize().width;
                                    int height = parameters.getPreviewSize().height;
                                    YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

                                    byte[] bytes = out.toByteArray();
                                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    Bitmap photo = createPhotos(bitmap);
                                    Bitmap roi = Bitmap.createBitmap(photo, (int) (0.2 * height), (int) (0.2 * width), (int) (0.6 * height), (int) (0.05 * width));/////
                                    roi = roi.copy(Bitmap.Config.ARGB_8888, true);
                                    baseApi.setImage(roi);
                                    final String recognizedText = baseApi.getUTF8Text();
                                    System.out.println(recognizedText);
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            try {
                                                String translation = Translator.translate(recognizedText);
                                                Message message = Message.obtain();
                                                message.obj = translation;
                                                handler.sendMessage(message);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            });
                        mode = 1;
                        button_back.setText("Back");
                        return;
                        } else {
                            mPreview.getHolder().removeCallback(mPreview);
                            mCamera.stopPreview();
                            mCamera.setPreviewCallback(null);
                            mCamera.release();
                            mCamera = null;
                            button_back.setText("Start");
                            mode = 0;
                            Intent intent = new Intent();
                            intent.setClass(RealtimeActivity.this, MainMenu.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
        );
    }

    public static Bitmap createPhotos(Bitmap bitmap) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            try {
                m.setRotate(90, bitmap.getWidth() / 2, bitmap.getHeight() / 2);//90就是我们需要选择的90度
                Bitmap bmp2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                bitmap.recycle();
                bitmap = bmp2;
            } catch (Exception ex) {
                System.out.print("Failed" + ex);
            }
        }
        return bitmap;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mode = 0; /*baseApi.end();*/
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
