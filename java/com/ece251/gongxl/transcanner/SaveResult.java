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

//    private static BluetoothService bluetoothService;
    private final static int REQUEST_FIND_DEVICES = 0;
    private final static int REQUEST_CANVAS = 1;
    String connectedDeviceName;
    private BluetoothService bluetoothService;
    String content;
    String filepath;

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
        filepath = fileIntent.getStringExtra("FilePath");
        content = fileIntent.getStringExtra("Content");
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

        bluetoothService = BluetoothService.getBluetoothService(getApplicationContext(),
                                                                handler);

        bluetoothService.switchBluetooth(BluetoothService.SWITCH_ON);
        Log.i("Bluetooth", "Enable bluetooth");

        bluetoothService.makeDiscoverable();
        Log.i("Bluetooth", "Visible");

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
                Intent findDeviceIntent = new Intent();
                findDeviceIntent.setClass(SaveResult.this,
                        ListDeviceActivity.class);
                startActivityForResult(findDeviceIntent, REQUEST_FIND_DEVICES);

                Log.i("Bluetooth", "Find device");
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
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == REQUEST_FIND_DEVICES) {
//            switch(resultCode) {
//                case Activity.RESULT_CANCELED:
//                    Toast.makeText(this,
//                            R.string.exit_find_device,
//                            Toast.LENGTH_LONG).show();
//                    break;
//                case Activity.RESULT_OK:
//                    Toast.makeText(this,
//                            R.string.connecting,
//                            Toast.LENGTH_LONG).show();
//                    connectDevice(data);
//
//                    break;
//            }
//        }
//    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        // Performing this check in onResume() covers the case in which BT was
//        // not enabled during onStart(), so we were paused to enable it...
//        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
//        if (bluetoothService != null && bluetoothService.isOn()) {
//            // Only if the state is STATE_NONE, do we know that we haven't started already
//            if (bluetoothService.getState() == BluetoothService.STATE_IDLE) {
//                // Start the Bluetooth chat services
//                bluetoothService.startListening();
//
//            }
//        }
////        else {
////            Toast.makeText(this,
////                    R.string.prompt_turn_on,
////                    Toast.LENGTH_LONG).show();
////        }
//    }
//
//    private void connectDevice(Intent data) {
//        // Get the device MAC address
//        String address = data.getExtras()
//                .getString(ListDeviceActivity.EXTRA_DEVICE_ADDRESS);
//
//        // Attempt to connect to the device
//        bluetoothService.startConnecting(address);
//    }
//
//
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.arg1) {
//                case BluetoothService.MESSAGE_STATE_CHANGE:
//                    switch (Integer.valueOf((String)msg.obj)) {
//                        case BluetoothService.STATE_COMMUNICATING:
//                            Toast.makeText(getApplicationContext(),
//                                    "Connected to " + connectedDeviceName,
//                                    Toast.LENGTH_SHORT).show();
//                            bluetoothService.sendFile(content);
//                            Log.i("Bluetooth", "Send successfully");
//                            Toast.makeText(getApplicationContext(),
//                                    "Finished! " + connectedDeviceName,
//                                    Toast.LENGTH_SHORT).show();
//
//                            break;
//                        case BluetoothService.STATE_CONNECTING:
//                            Toast.makeText(getApplicationContext(),
//                                    R.string.connecting,
//                                    Toast.LENGTH_SHORT).show();
//                            break;
//                        case BluetoothService.STATE_LISTENING:
////                            Toast.makeText(getApplicationContext(),
////                                    R.string.prompt_listening,
////                                    Toast.LENGTH_SHORT).show();
//                            break;
//                        case BluetoothService.STATE_IDLE:
//                            if(bluetoothService.isOn()) {
//                                bluetoothService.startListening();
//                            }
//                            break;
//                    }
//                    break;
//                case BluetoothService.MESSAGE_DEVICE_NAME:
//                    connectedDeviceName = (String) msg.obj;
//                    break;
//                case BluetoothService.MESSAGE_CONNECTION_FAILED:
//                    Toast.makeText(getApplicationContext(),
//                            R.string.connection_fail,
//                            Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothService.MESSAGE_CONNECTION_LOST:
//                    Toast.makeText(getApplicationContext(),
//                            R.string.connection_lost,
//                            Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothService.MESSAGE_READ:
//                    String text = (String) msg.obj;
//                    // construct a string from the valid bytes in the buffer
//
//                    Toast.makeText(getApplicationContext(),
//                            R.string.prompt_receive_message,
//                            Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothService.MESSAGE_WRITE:
//                    String echo = (String) msg.obj;
//                    System.out.println("message signal received" + echo);
//                    if(echo == "EOF")
//                    Toast.makeText(getApplicationContext(),
//                            "Finish sending!",
//                            Toast.LENGTH_LONG).show();
//                    break;
//            }
//        }
//    };
//
    @Override
    public void onStop(){
        super.onStop();
        locationService.stopService();
        bluetoothService.stopService();
    }
//
//    static BluetoothService getBluetoothService(){
//        return bluetoothService;
//    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (Integer.valueOf((String)msg.obj)) {
                        case BluetoothService.STATE_COMMUNICATING:
                            Toast.makeText(getApplicationContext(),
                                    "Connected to " + connectedDeviceName,
                                    Toast.LENGTH_SHORT).show();
                            bluetoothService.sendFile(content);
                            Log.i("Bluetooth", "Send successfully");
                            Toast.makeText(getApplicationContext(),
                                    "Finished! ",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(),
                                    R.string.connecting,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_LISTENING:
                            Toast.makeText(getApplicationContext(),
                                    R.string.prompt_listening,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_IDLE:
                            if(bluetoothService.isOn()) {
                                bluetoothService.startListening();
                            }
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    connectedDeviceName = (String) msg.obj;
                    break;
                case BluetoothService.MESSAGE_CONNECTION_FAILED:
                    Toast.makeText(getApplicationContext(),
                            R.string.connection_fail,
                            Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(),
                            R.string.connection_lost,
                            Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_READ:
                    String text = (String) msg.obj;
                    System.out.println(text);
                    // construct a string from the valid bytes in the buffer

                    Toast.makeText(getApplicationContext(),
                            R.string.prompt_receive_message,
                            Toast.LENGTH_SHORT).show();


                    break;
                case BluetoothService.MESSAGE_WRITE:
                    String echo = (String) msg.obj;
                    System.out.println("message signal received" + echo);
                    Toast.makeText(getApplicationContext(),
                            "Message " + echo + "sent",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
