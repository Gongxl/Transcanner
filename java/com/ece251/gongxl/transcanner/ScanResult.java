package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ScanResult extends Activity {
    Handler handler;
    private PopupWindow save_popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        //set textview context

        final EditText editText = (EditText)findViewById(R.id.scan_result);
        Intent intent = getIntent();
        final String ocr_result = intent.getStringExtra("OCR");
        editText.setText(ocr_result);

        ImageButton b1 = (ImageButton)findViewById(R.id.home);
        b1.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(ScanResult.this,MainMenu.class);
                startActivity(intent);
                finish();
            }
        });


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);


                editText.setText((String) msg.obj);
            }
        };
        
        ImageButton b2 = (ImageButton)findViewById(R.id.translate);
        b2.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                String translated = Translator.translate(editText.getText().toString());
                                Message message = Message.obtain();
                                message.obj = translated;
                                handler.sendMessage(message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
            }
        });

        ImageButton b3 = (ImageButton)findViewById(R.id.save);
        b3.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


//                if (save_popup != null&&save_popup.isShowing()) {
//                    save_popup.dismiss();
//                    return;
//                } else {
//                    initmPopupWindowView();
//
//                    save_popup.showAtLocation(findViewById(R.id.save),Gravity.BOTTOM,0,0);
//                }
                EditText editText = (EditText)findViewById(R.id.scan_result);
                Log.i("editText",editText.getText().toString());
                String filepath = SavetoFile(editText.getText().toString(),false).getPath();
                Toast.makeText(ScanResult.this,"Saved",Toast.LENGTH_LONG).show();
                String shootTime = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss z").format(new Date());
                Intent intent = new Intent(ScanResult.this,SaveResult.class);
                intent.putExtra("Content",editText.getText().toString()+"\n");
                intent.putExtra("FilePath",filepath);
                intent.putExtra("shootTime",shootTime);
                startActivity(intent);
                finish();
            }
        });
    }

    public void initmPopupWindowView() {
        View customView = null;
        customView = getLayoutInflater().inflate(R.layout.save_popup, null, false);
        save_popup = new PopupWindow(customView, 450, 150);
        // 使其聚集 要想监听菜单里控件的事件就必须要调用此方法
        save_popup.setFocusable(true);
        save_popup.setAnimationStyle(R.style.AnimationPreview);
        // 自定义view添加触摸事件
        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (save_popup != null && save_popup.isShowing()) {
                    save_popup.dismiss();
                    save_popup = null;
                }
                return false;
            }
        });
        LinearLayout layoutEffect1 = (LinearLayout) customView.findViewById(R.id.layout_effect_hj);
        layoutEffect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanResult.this, "效果-怀旧", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout layoutEffect2 = (LinearLayout) customView.findViewById(R.id.layout_effect_fd);
        layoutEffect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanResult.this, "效果-浮雕", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout layoutEffect3 = (LinearLayout) customView.findViewById(R.id.layout_effect_gz);
        layoutEffect3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanResult.this, "效果-光照", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected static File SavetoFile(String content, Boolean rec){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File sdCardDir = Environment.getExternalStorageDirectory();
            File StorageDir = new File(sdCardDir,"TS");
            if (! StorageDir.exists()){
                if (! StorageDir.mkdirs()){
                    Log.d("Save", "failed to create directory");
                    return null;
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File saveFile;
            if(!rec)
            saveFile = new File(StorageDir.getPath(), File.separator + timeStamp + ".txt");
            else saveFile = new File(StorageDir.getPath(), File.separator + "Rec_" + timeStamp + ".txt");


            FileOutputStream outStream;
            try {
                outStream = new FileOutputStream(saveFile);
                outStream.write(content.getBytes());
                outStream.close();


            } catch (Exception e) {
                e.printStackTrace();
            }

            return saveFile;
        }
        else return null;
    }


}
