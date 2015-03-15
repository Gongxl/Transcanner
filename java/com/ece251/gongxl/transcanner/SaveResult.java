package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class SaveResult extends Activity {

    private LocationService locationService;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_result);

        Intent fileIntent = getIntent();
        final String filepath = fileIntent.getStringExtra("FilePath");

        locationService = new LocationService(getApplicationContext());
        locationService.startService();

        final TextView textView  = (TextView) findViewById(R.id.location);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                textView.setText((String) msg.obj);
                locationService.stopService();
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    String address = locationService.getAddress();
                    Message message = Message.obtain();
                    message.obj = address;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        LinearLayout l1 = (LinearLayout)findViewById(R.id.layout_home);
        l1.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SaveResult.this,MainMenu.class);
                startActivity(intent);
                finish();
            }
        });

        LinearLayout l2 = (LinearLayout)findViewById(R.id.layout_bluetooth);
        l2.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SaveResult.this,BluetoothActivity.class);
                intent.putExtra("FilePath",filepath);
                Log.i("Bluetooth",filepath);
                startActivity(intent);
                finish();
            }
        });

        LinearLayout l3 = (LinearLayout)findViewById(R.id.layout_email);
        l3.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.i("Send email", "Begin!");


                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
                intent.putExtra(Intent.EXTRA_TEXT, "body");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filepath));//file path
                intent.setType("image/*");
                intent.setType("message/rfc882");

                try {
                    startActivity(Intent.createChooser(intent, "Choose Email Client"));

                    Log.i("Finished sending email.", "");
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SaveResult.this,
                            "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


}
