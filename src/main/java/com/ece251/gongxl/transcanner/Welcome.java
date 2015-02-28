package com.ece251.gongxl.transcanner;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Welcome extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(Welcome.this,MainMenu.class));
                finish();

            }
        }, 2000);//这里停留时间为1000=1s。
    }

}
