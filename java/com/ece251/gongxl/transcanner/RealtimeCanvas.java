package com.ece251.gongxl.transcanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public class RealtimeCanvas extends ActionBarActivity {
    private static final int REQUEST_CANVAS = 1;
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
        bluetoothService = MainMenu.getBluetoothService();
        bluetoothService.switchBluetooth(BluetoothService.SWITCH_ON);
        Log.i("Canvas", "Enable bluetooth");
        bluetoothService.makeDiscoverable();
        Log.i("Canvas", "Visible");
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
            Intent findDeviceIntent = new Intent();
            findDeviceIntent.setClass(RealtimeCanvas.this,
                    ListDeviceActivity.class);
            startActivityForResult(findDeviceIntent, REQUEST_CANVAS);

            Log.i("Bluetooth", "Find device");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop(){
        super.onStop();
        bluetoothService.stopService();
    }
}

