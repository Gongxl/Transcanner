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
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class SaveResult extends Activity {

    private LocationService locationService;
    private TabHost myTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_result);
        myTabHost = (TabHost) findViewById(android.R.id.tabhost);
        myTabHost.setup();

        final TextView t1 = (TextView)findViewById(R.id.file_name);
        final TextView t2 = (TextView)findViewById(R.id.time_date);
        final TextView t3 = (TextView)findViewById(R.id.location);

        Intent fileIntent = getIntent();
        final String filepath = fileIntent.getStringExtra("FilePath");
        final String content = fileIntent.getStringExtra("Content");
        t1.setText(filepath);
        String shootTime = fileIntent.getStringExtra("shootTime");
        t2.setText(shootTime);

        this.locationService = new LocationService(getApplicationContext());
        locationService.startService();

        myTabHost.addTab(myTabHost.newTabSpec("tab1").setIndicator("Name & Path").setContent(R.id.tab1));
        myTabHost.addTab(myTabHost.newTabSpec("tab2").setIndicator("Time & Date").setContent(R.id.tab2));
        myTabHost.addTab(myTabHost.newTabSpec("tab3").setIndicator("Location").setContent(R.id.tab3));

        myTabHost.getTabWidget().getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t3.setText(locationService.getAddress());
                myTabHost.setCurrentTab(2);
            }
        });

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
                intent.putExtra("Content",content);
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
