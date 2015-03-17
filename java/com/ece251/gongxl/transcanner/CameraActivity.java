package com.ece251.gongxl.transcanner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {

    private Camera mCamera;
    public static int screenWidth;
    public static int screenHeight;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    final CharSequence str_addmask = "Add Mask";
    public static int mode = 0; // 0 preivew 1 addmask 2 return
    public static Bitmap image;
    public static int num=0;
    private AutoFocusCallback myAutoFocusCallback = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        screenWidth  = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();      // 屏幕高（像素，如：800p）
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        final Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if(mode == 0){
                            PictureCallback mPicture = new PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {

                                    try {
                                        FileOutputStream fos = new FileOutputStream("/mnt/sdcard/test.jpg");
                                        //image = BitmapFactory.decodeByteArray(data , 0, data.length);
                                        fos.write(data);
                                        fos.close();

                                        Intent intent = new Intent();
                                        intent.setClass(CameraActivity.this, DrawActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } catch (FileNotFoundException e) {
                                        Log.d("TAG", "File not found: " + e.getMessage());
                                    } catch (IOException e) {
                                        Log.d("TAG", "Error accessing file: " + e.getMessage());
                                    }
                                }
                            };

                            myAutoFocusCallback = new AutoFocusCallback() {

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
                            mCamera.autoFocus(myAutoFocusCallback);
                            mCamera.takePicture(null, null, mPicture);
                        }

                    }
                }
        );
    }



    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
