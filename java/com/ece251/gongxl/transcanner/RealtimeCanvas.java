package com.ece251.gongxl.transcanner;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public class RealtimeCanvas extends ActionBarActivity {
    private LinearLayout canvas;
    private CanvasView canvasView;
    private BluetoothService bluetoothService;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_canvas);
        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.arg1) {
                    case BluetoothService.MESSAGE_DRAWING:
                        System.out.println("draw message received");
                        canvasView.addDrawing((String) msg.obj);
                        break;
                }
            }
        };
        bluetoothService = MainActivity.getBluetoothService();
        bluetoothService.send("canvas view");
        bluetoothService.setCanvasHandler(handler);
        canvas = (LinearLayout) findViewById(R.id.canvasView);
        canvasView = new CanvasView(getApplicationContext(), bluetoothService);
        canvas.addView(canvasView);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_realtime_canvas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

