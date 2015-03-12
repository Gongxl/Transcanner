package com.ece251.gongxl.transcanner;

/**
 * Created by david on 3/11/15.
 */
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    private Switch bluetoothSwitch;
    private Button visiblity;
    private Button findDevices;
    private Button sendButton;
    private Button disconnect;
    private EditText sendBox;
    private TextView receiveMessage;
    private BluetoothService bluetoothService;
    private final static int REQUEST_FIND_DEVICES = 0;
    String connectedDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothService = new BluetoothService(this, handler);
        sendButton = (Button) findViewById(R.id.sendMessage);
        visiblity = (Button) findViewById(R.id.visibility);
        bluetoothSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
        findDevices = (Button) findViewById(R.id.findDevices);
        disconnect = (Button) findViewById(R.id.disconnect);
        sendBox = (EditText) findViewById(R.id.sendBox);
        receiveMessage = (TextView) findViewById(R.id.receivedMessage);

        visiblity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.makeDiscoverable();
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.stopService();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = sendBox.getText().toString();
                bluetoothService.send(text);
                sendBox.setText("");
            }
        });

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    bluetoothService.switchBluetooth(BluetoothService.SWITCH_ON);
                else bluetoothService.switchBluetooth(BluetoothService.SWITCH_OFF);
            }
        });

        findDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findDeviceIntent = new Intent();
                findDeviceIntent.setClass(MainActivity.this,
                        ListDeviceActivity.class);
                startActivityForResult(findDeviceIntent, REQUEST_FIND_DEVICES);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                    // construct a string from the valid bytes in the buffer
                    receiveMessage.setText(connectedDeviceName
                            + ":  " + text);
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
