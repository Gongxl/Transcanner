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
import android.widget.CheckBox;
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
    String orig;
    Boolean atTrans = false;
    private Handler trans_handler;
    private Translator translator;
//    private Boolean autoC = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        //set textview context

        final EditText editText = (EditText)findViewById(R.id.scan_result);
        Intent intent = getIntent();
        final String ocr_result = intent.getStringExtra("OCR");
        editText.setText(ocr_result);
        orig = ocr_result;

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


        ImageButton b2 = (ImageButton)findViewById(R.id.translate);
        b2.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!atTrans) {
                   orig = editText.getText().toString();
                   translator.translate(editText.getText().toString(),false);
                   atTrans = true;
                }
            }
        });

        ImageButton b3 = (ImageButton)findViewById(R.id.save);
        b3.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


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

        ImageButton b4 = (ImageButton)findViewById(R.id.backtoscan);
        b4.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                editText.setText(orig);
                atTrans = false;
            }
        });

        final CheckBox checkBox = (CheckBox)findViewById(R.id.auto_correct);
        checkBox.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View v){
                if(checkBox.isChecked()){
//                    autoC = true;

                    translator.autoCorrect(editText.getText().toString());
                }
                else if(orig!=null)editText.setText(orig);
            }
        });

        trans_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.arg1 == Translator.MESSAGE_AUTOCORRECT) {
                    //editText.setText((String) msg.obj);
                    System.out.println("received autocorrect msg" + (String) msg.obj);
                    editText.setText((String)msg.obj);
                }
                else if(msg.arg1 == Translator.MESSAGE_TRANSLATE){

                    System.out.println("received translate msg");
                    editText.setText((String) msg.obj);
                } else {

                    System.out.println("received lookup msg");

                }
            }
        };
        translator = new Translator(getApplicationContext(), trans_handler);

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
