package com.ece251.gongxl.transcanner;

/**
 * Created by hch on 2015/3/1.
 */


        import java.io.BufferedOutputStream;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;

        import java.io.InputStream;
        import java.io.OutputStream;
        import java.text.SimpleDateFormat;
        import java.util.Date;

        import android.content.res.AssetManager;
        import android.support.v7.app.ActionBarActivity;
        import android.support.v7.app.ActionBar;
        import android.support.v4.app.Fragment;
        import android.app.Activity;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.graphics.PointF;
        import android.hardware.Camera;
        import android.hardware.Camera.AutoFocusCallback;
        import android.hardware.Camera.PictureCallback;
        import android.media.FaceDetector;
        import android.os.Bundle;
        import android.os.Environment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.Button;
        import android.widget.FrameLayout;
        import android.os.Build;


        import com.googlecode.tesseract.android.TessBaseAPI;


public class DrawActivity extends Activity {


    public static Bitmap image;
    public static Bitmap photo;
    //public static Mask mask;
    public static DrawboxView drawboxView;
    public static int screenWidth;
    public static int screenHeight;
    private Button button = null;
    public static float pleft, pright, ptop, pbottom;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";

    public static final String lang = "eng";
    private static final String TAG = "DrawActivity.java";

    public static Bitmap createPhotos(Bitmap bitmap){
        if(bitmap!=null){
            Matrix m=new Matrix();
            try{
                m.setRotate(90, bitmap.getWidth()/2, bitmap.getHeight()/2);//90就是我们需要选择的90度
                Log.i("TAG", "W/H/w/h " +  screenWidth + screenHeight + bitmap.getHeight() + bitmap.getWidth());
                float scale_width = (float)screenWidth / (float)bitmap.getHeight();
                float scale_height = (float)screenHeight / (float)bitmap.getWidth();
                Log.i("TAG", "scale w/h" + scale_width + " "+ scale_height);
                m.postScale(scale_width, scale_height);
                Bitmap bmp2=Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                bitmap.recycle();
                bitmap=bmp2;
            }catch(Exception ex){
                System.out.print("Failed"+ex);
            }
        }
        return bitmap;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {


        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

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

        Log.i("TAG","DrawActivity!");

        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawactivity);
        screenWidth  = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();      // 屏幕高（像素，如：800p）
        //mask = (Mask)findViewById(R.id.mask);
        drawboxView = (DrawboxView)findViewById(R.id.drawboxview);

        BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
        BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;  //构造位图生成的参数，必须为565。类名+enum
        image = BitmapFactory.decodeFile("/mnt/sdcard/test.jpg", BitmapFactoryOptionsbfo);
        photo = createPhotos(image);
/*
        Intent intent=getIntent();
        if(intent!=null)
        {
            photo=intent.getParcelableExtra("bitmap");
        }*/
        drawboxView.showFrame(photo);

        button = (Button)findViewById(R.id.translate);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pleft = drawboxView.getLeftPos();
                        pright = drawboxView.getRightPos();
                        ptop = drawboxView.getTopPos();
                        pbottom = drawboxView.getBottomPos();

                        Bitmap roi = Bitmap.createBitmap(photo, (int)pleft,(int)ptop,(int)(pright-pleft), (int)(pbottom-ptop));
                        roi = roi.copy(Bitmap.Config.ARGB_8888, true);

                        TessBaseAPI baseApi = new TessBaseAPI();
                        baseApi.setDebug(true);
                        baseApi.init(DATA_PATH, lang);
                        baseApi.setImage(roi);

                        String recognizedText = baseApi.getUTF8Text();

                        Intent intent = new Intent(DrawActivity.this,ScanResult.class);
                        intent.putExtra("OCR", recognizedText);
                        startActivity(intent);

                        baseApi.end();
                        finish();
                    }
                }
        );


    }

}

