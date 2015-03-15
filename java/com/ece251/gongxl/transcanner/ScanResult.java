package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ScanResult extends Activity {
        Handler handler;

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
                String filepath = SavetoFile().getPath();
                Intent intent = new Intent(ScanResult.this,SaveResult.class);

                intent.putExtra("FilePath",filepath);
                startActivity(intent);
                finish();
            }
        });
    }

    protected File SavetoFile(){
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
            File saveFile = new File(StorageDir.getPath(), File.separator + timeStamp + ".txt");

            EditText editText = (EditText)findViewById(R.id.scan_result);
            FileOutputStream outStream;
            try {
                outStream = new FileOutputStream(saveFile);
                outStream.write(editText.getText().toString().getBytes());
                outStream.close();
                Toast.makeText(ScanResult.this,"Saved",Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return saveFile;
        }
        else return null;
    }


}
