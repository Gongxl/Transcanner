package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


public class TransMode extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_mode);
        LinearLayout l_sentence = (LinearLayout)findViewById(R.id.sentence);
        LinearLayout l_word = (LinearLayout)findViewById(R.id.word);
        l_sentence.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(TransMode.this,RealtimeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        l_word.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(TransMode.this,DicActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
