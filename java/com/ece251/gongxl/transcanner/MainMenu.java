package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;


public class MainMenu extends Activity {
    private static BluetoothService bluetoothService;
    private final static int REQUEST_FIND_DEVICES = 0;
    private final static int REQUEST_CANVAS = 1;
    String connectedDeviceName;
    String content;

    ListView list;
    String[] name = {
            "SCAN",
            "TRANSLATE",
            "IMPORT",
            "DRAW",
            "ABOUT US",

    } ;
    Integer[] imageId = {
            R.drawable.scan2,
            R.drawable.trans2,
            R.drawable.inbox,
            R.drawable.draw,
            R.drawable.info2,

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        bluetoothService = new BluetoothService(this, handler);
        CustomList adapter = new
                CustomList(MainMenu.this, name, imageId);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.i("position", String.valueOf(position));
                Intent intent;
                switch (position) {
                    case 0: {
                        intent = new Intent(MainMenu.this, CameraActivity.class);       ////// huangchong
                        startActivity(intent);
                        break;
                    }
                    case 1: {
                        intent = new Intent(MainMenu.this, RealtimeActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 2: {
                        intent = new Intent(MainMenu.this, ImportActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 3: {
                        intent = new Intent(MainMenu.this,RealtimeCanvas.class);
                        startActivity(intent);
                        break;
                    }
                    case 4: {
                        LayoutInflater layoutInflater
                                = (LayoutInflater) getBaseContext()
                                .getSystemService(LAYOUT_INFLATER_SERVICE);
                        View popupView = layoutInflater.inflate(R.layout.info_popup, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                        btnDismiss.setOnClickListener(new Button.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                popupWindow.dismiss();
                            }
                        });
                        popupWindow.showAtLocation(findViewById(R.id.menu), Gravity.CENTER, 0, 0);

                    }
                }
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
        }
//        else {
//            Toast.makeText(this,
//                    R.string.prompt_turn_on,
//                    Toast.LENGTH_LONG).show();
//        }
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
                            System.out.println("In MainMenu: startCommunication!");
                            Toast.makeText(getApplicationContext(),
                                    "Connected to " + connectedDeviceName,
                                    Toast.LENGTH_SHORT).show();
//                            bluetoothService.sendFile(content);
//                            Log.i("Bluetooth", "Send successfully");
//                            Toast.makeText(getApplicationContext(),
//                                    "Finished! " + connectedDeviceName,
//                                    Toast.LENGTH_SHORT).show();

                            break;
                        case BluetoothService.STATE_CONNECTING:

                            System.out.println("In MainMenu: startConnection!");
                            Toast.makeText(getApplicationContext(),
                                    R.string.connecting,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_LISTENING:

                            System.out.println("In MainMenu: startListening!");
//                            Toast.makeText(getApplicationContext(),
//                                    R.string.prompt_listening,
//                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_IDLE:

                            System.out.println("In MainMenu: startIdle!");
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
                    if(echo == "EOF")
                        Toast.makeText(getApplicationContext(),
                                "Finish sending!",
                                Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    static BluetoothService getBluetoothService(){
        return bluetoothService;
    }
}
