package com.ece251.gongxl.transcanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class ImportActivity extends ListActivity {
    private TextView receiveMessage;
    private BluetoothService bluetoothService;
    private final static int REQUEST_FIND_DEVICES = 0;
    String connectedDeviceName;

    private ArrayList<String> items = null;
    private ArrayList<String> paths = null;
    private String rootPath = "/";
    private TextView mPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        bluetoothService = BluetoothService.getBluetoothService(getApplicationContext(),handler);

        receiveMessage = (TextView) findViewById(R.id.receivedMessage);

        bluetoothService.switchBluetooth(BluetoothService.SWITCH_ON);

        bluetoothService.makeDiscoverable();

        mPath = (TextView)findViewById(R.id.mPath);
        mPath.setTextColor(Color.RED);

        rootPath = Environment.getExternalStorageDirectory().getPath()+"/TS";
        getFileDir(rootPath);

        ImageButton refresh = (ImageButton)findViewById(R.id.update);
        refresh.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                getFileDir(rootPath);
            }
        });

    }

    private void getFileDir(String filePath) {
        mPath.setText(filePath);

        items = new ArrayList<String>();
        paths = new ArrayList<String>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if(!filePath.equals(rootPath)) {
            items.add("Back To " + rootPath);
            paths.add(rootPath);
            items.add("Back to ../");
            paths.add(file.getParent());
        }
        for(File fileTemp :files) {
            items.add(fileTemp.getName());
            paths.add(fileTemp.getPath());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ImportActivity.this,R.layout.file_now,items);
        setListAdapter(adapter);


    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = new File(paths.get(position));
        if(file.canRead()) {
            if(file.isDirectory()) {
                getFileDir(paths.get(position));
            }else {
                new AlertDialog.Builder(this)
                        .setTitle("Message")
                        .setMessage("["+file.getName() + "] is a file")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

            }
        }else {
            new AlertDialog.Builder(this)
                    .setTitle("Message")
                    .setMessage("Access denied")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();

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
