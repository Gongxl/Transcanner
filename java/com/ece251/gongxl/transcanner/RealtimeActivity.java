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
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.googlecode.tesseract.android.TessBaseAPI;


public class RealtimeActivity extends ActionBarActivity {

    private Camera mCamera;
    public static int screenWidth;
    public static int screenHeight;
    private CameraPreview mPreview;
    private FrameLayout preview;
    public static Bitmap image;
    public static int num = 0;
    public static int mode = 0;
    private int frameCount = 0;
    public Paint mPaint = new Paint();
    private int bleft, bright, btop, bbottom;
    private Button button_back;
    private BoxView boxView;
    private Translator translator;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
    private Camera.AutoFocusCallback myAutoFocusCallback = null;
    public static final String lang = "eng";
    private static final String TAG = "RealtimeActivity.java";
    public TessBaseAPI baseApi;
    private Handler handler;

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
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();      // 屏幕高（像素，如：800p）
        bleft = (int) (screenWidth * 0.2);
        bright = (int) (screenWidth * 0.8);
        btop = (int) (screenHeight * 0.2);
        bbottom = (int) (screenHeight * 0.25); /////

        setContentView(R.layout.realtimeactivity);

        initializeOCR();
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

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.arg1 == Translator.MESSAGE_AUTOCORRECT) {
                    //editText.setText((String) msg.obj);
                    System.out.println("received autocorrect msg" + (String) msg.obj);
                }
                else if(msg.arg1 == Translator.MESSAGE_TRANSLATE){
                    boxView.displayText((String) msg.obj);
                    System.out.println("received translate msg");
                } else {
                    boxView.displayText((String) msg.obj);
                    System.out.println("received lookup msg");
                }
            }
        };
        translator = new Translator(getApplicationContext(), handler);

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
                            mCamera.autoFocus(myAutoFocusCallback);
                            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                                public void onPreviewFrame(byte[] data, Camera camera) {
//                                    if(frameCount ++ != 10) return;
//                                    frameCount = 0;
                                    Camera.Parameters parameters = camera.getParameters();
                                    int width = parameters.getPreviewSize().width;
                                    int height = parameters.getPreviewSize().height;
                                    YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
                                    //mCamera.autoFocus(myAutoFocusCallback);
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

                                    byte[] bytes = out.toByteArray();
                                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    Bitmap photo = createPhotos(bitmap);
                                    Bitmap roi = Bitmap.createBitmap(photo, (int) (0.2 * height), (int) (0.2 * width), (int) (0.6 * height), (int) (0.05 * width));/////
                                    roi = roi.copy(Bitmap.Config.ARGB_8888, true);

                                    baseApi.setImage(roi);
                                    String recognizedText = baseApi.getUTF8Text();
                                    System.out.println(frameCount ++);
                                    System.out.println("creating new thread");
                                    translator.translate(recognizedText, true);
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
                            intent.setClass(RealtimeActivity.this,MainMenu.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
        );
        myAutoFocusCallback = new Camera.AutoFocusCallback() {

            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                if(success)//success表示对焦成功
                {
                    Log.i("TAG", "myAutoFocusCallback: success...");
                    //myCamera.setOneShotPreviewCallback(null);

                }
                else
                {
                    //未对焦成功
                    Log.i("TAG", "myAutoFocusCallback: fail...");

                }


            }
        };
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

    private class TransTask extends AsyncTask<Void, Void, Void> {

        private byte[] mData;

        //构造函数
        TransTask(byte[] data) {
            this.mData = data;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            num++;
            Log.d("===>", "onPreviewFrame" + num);
            //textView.setText("onPreviewFrame" + num);
            return null;
        }

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_realtime_trans, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.jump_dic) {
            Intent intent = new Intent(RealtimeActivity.this,DicActivity.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.back_home) {
            Intent intent = new Intent(RealtimeActivity.this,MainMenu.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}

