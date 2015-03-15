package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



public class ImportActivity extends Activity {
    private TextView receiveMessage;
    private BluetoothService bluetoothService;
    private final static int REQUEST_FIND_DEVICES = 0;
    String connectedDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        bluetoothService = new BluetoothService(this, handler);

        receiveMessage = (TextView) findViewById(R.id.receivedMessage);

        bluetoothService.switchBluetooth(BluetoothService.SWITCH_ON);

        bluetoothService.makeDiscoverable();


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
                    receiveMessage.setText(connectedDeviceName
                            + ":  " + text);
                    Toast.makeText(getApplicationContext(),
                            R.string.prompt_receive_message,
                            Toast.LENGTH_SHORT).show();
                    receiveMessage.setText("Received!");

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
