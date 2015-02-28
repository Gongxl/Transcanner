package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.logging.LogRecord;


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
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);


                editText.setText((String) msg.obj);
            }
        };
        final String trans="Fifty shades of Grey";
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
    }


}
