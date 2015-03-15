package com.ece251.gongxl.transcanner;

/**
 * Created by david on 3/11/15.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends Activity {

    private static BluetoothService bluetoothService;
    private final static int REQUEST_FIND_DEVICES = 0;
    private final static int REQUEST_CANVAS = 1;
    String connectedDeviceName;
    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Intent fileIntent = getIntent();
        content = fileIntent.getStringExtra("Content");
        Log.i("Content",content);

        bluetoothService = new BluetoothService(this, handler);


        bluetoothService.switchBluetooth(BluetoothService.SWITCH_ON);
        Log.i("Bluetooth", "Enable bluetooth");
//        try {
//            Thread.currentThread().sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        bluetoothService.makeDiscoverable();
        Log.i("Bluetooth", "Visible");

        Button find_btn = (Button)findViewById(R.id.find);
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findDeviceIntent = new Intent();
                findDeviceIntent.setClass(BluetoothActivity.this,
                        ListDeviceActivity.class);
                startActivityForResult(findDeviceIntent, REQUEST_FIND_DEVICES);

                Log.i("Bluetooth", "Find device");
            }
        });

        Button canvas_btn = (Button)findViewById(R.id.canvas);
        canvas_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findDeviceIntent = new Intent();
                findDeviceIntent.setClass(BluetoothActivity.this,
                       RealtimeCanvas.class);
                startActivityForResult(findDeviceIntent, REQUEST_CANVAS);

                Log.i("Bluetooth", "Find device");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_FIND_DEVICES) {
            switch(resultCode) {
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this,
                            R.string.exit_find_device,
                            Toast.LENGTH_LONG).show();
                    break;
                case Activity.RESULT_OK:
                    Toast.makeText(this,
                            R.string.connecting,
                            Toast.LENGTH_LONG).show();
                    connectDevice(data);

                    break;
            }
        }
    }
    @Override
    public void onStop(){
        super.onStop();
        bluetoothService.stopService();
    }
    @Override
    public void onStart() {
        super.onStart();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (bluetoothService != null && bluetoothService.isOn()) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService.getState() == BluetoothService.STATE_IDLE) {
                // Start the Bluetooth chat services
                bluetoothService.startListening();

            }
        } else {
            Toast.makeText(this,
                    R.string.prompt_turn_on,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(ListDeviceActivity.EXTRA_DEVICE_ADDRESS);

        // Attempt to connect to the device
        bluetoothService.startConnecting(address);
    }


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

                                    TextView textView = (TextView)findViewById(R.id.bluetooth_finish);
                                    textView.setText("Finish!");

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

    static BluetoothService getBluetoothService(){
        return bluetoothService;
    }
}
