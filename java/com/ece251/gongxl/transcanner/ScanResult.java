package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


public class ScanResult extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        //set textview context
        final TextView textView = (TextView)findViewById(R.id.scan_result);
//        textView.setText(...);
        final String trans="Fifty shades of Grey";
        Button b2 = (Button)findViewById(R.id.translate);
        b2.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    
                    textView.setText(Translator.translate(trans));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
