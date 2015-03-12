package com.ece251.gongxl.transcanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;


/**
 * Created by david on 3/9/15.
 */
public class BluetoothService {
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final String SERVICE_NAME = "TRANSCANNER_SHARE";
    // Member fields
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private final Context context;
    /**
     * acceptThread is run in block mode, waiting
     * for the other devices connect request
     */
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private CommunicationThread communicationThread;
    private int serviceState;

    // Constants that indicate the current connection state
    // we're idle
    public static final int STATE_IDLE = 0;
    // now listening for incoming connections
    public static final int STATE_LISTENING = 1;
    // now initiating an outgoing connection
    public static final int STATE_CONNECTING = 2;
    // now connected to a remote device
    public static final int STATE_COMMUNICATING = 3;

    public static final boolean SWITCH_ON = true;
    public static final boolean SWITCH_OFF = false;



    // Constants that indicate the message
    public static final int MESSAGE_STATE_CHANGE = 0;
    public static final int MESSAGE_DEVICE_NAME = 1;
    public static final int MESSAGE_CONNECTION_FAILED = 2;
    public static final int MESSAGE_CONNECTION_LOST = 3;
    public static final int MESSAGE_READ = 4;
    public static final int MESSAGE_WRITE = 5;
    public static final int THREAD_LISTENING = 0;
    public static final int THREAD_CONNECTING = 1;
    public static final int THREAD_COMMUNICATION = 2;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.serviceState = STATE_IDLE;
        this.handler = handler;
        this.context = context;
    }

    public synchronized void startListening() {
        // Cancel any thread attempting to make a connection
        shutThread(THREAD_CONNECTING);

        // Cancel any thread currently running a connection
        shutThread(THREAD_COMMUNICATION);

        setState(STATE_LISTENING);

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public boolean isOn() {
        return bluetoothAdapter.isEnabled();
    }

    public void switchBluetooth(boolean flag) {
        if(flag == SWITCH_ON) {
            if(!bluetoothAdapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(turnOn);
                Toast.makeText(context,
                        R.string.turn_on,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context,
                        R.string.already_on,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            if(flag == SWITCH_OFF) {
                if(!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(context,
                            R.string.already_off,
                            Toast.LENGTH_LONG).show();
                } else {
                    bluetoothAdapter.disable();
                    Toast.makeText(context,
                            R.string.turn_off,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void makeDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            context.startActivity(discoverableIntent);
        }
    }
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void startConnecting(String address) {
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Cancel any thread attempting to make a connection
        shutThread(THREAD_CONNECTING);
        // Cancel any thread currently running a connection
        shutThread(THREAD_COMMUNICATION);

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();

        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void startCommunication(BluetoothSocket socket,
                                                BluetoothDevice device) {


        // Cancel the thread that completed the connection
        shutThread(THREAD_COMMUNICATION);
        // Cancel any thread currently running a connection
        shutThread(THREAD_CONNECTING);
        // Cancel the accept thread because we only want to
        // connect to one device
        shutThread(THREAD_LISTENING);

        // Start the thread to manage the connection and perform transmissions
        communicationThread = new CommunicationThread(socket);
        communicationThread.start();

        // Send the name of the connected device back to the UI Activity
        syncMessage(MESSAGE_DEVICE_NAME, device.getName());
        System.out.println("device name" + device.getName());
        setState(STATE_COMMUNICATING);
    }

    private void syncMessage(int messageType, String text) {
        Message message = Message.obtain();
        message.arg1 = messageType;
        message.obj = text;
        handler.sendMessage(message);
    }

    private synchronized void shutThread(int threadType) {
        switch (threadType) {
            case THREAD_LISTENING:
                if (acceptThread != null) {
                    acceptThread.terminate();
                    acceptThread = null;
                }
                break;
            case THREAD_CONNECTING:
                if (connectThread != null) {
                    connectThread.terminate();
                    connectThread = null;
                }
                break;
            case THREAD_COMMUNICATION:
                if (communicationThread != null) {
                    communicationThread.terminate();
                    communicationThread = null;
                }
                break;
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stopService() {
        Toast.makeText(context,
                R.string.prompt_disconnect,
                Toast.LENGTH_LONG).show();

        // Cancel the thread that completed the connection
        shutThread(THREAD_COMMUNICATION);
        // Cancel any thread currently running a connection
        shutThread(THREAD_CONNECTING);
        // Cancel the listening thread
        shutThread(THREAD_LISTENING);
        setState(STATE_IDLE);
    }

    /**
     * Send a string by writing to the CommunicationThread
     * in an unsynchronized manner
     */
    public void send(String toSent) {
        byte[] out = toSent.getBytes();
        // Create temporary object
        CommunicationThread threadCopy;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (serviceState != STATE_COMMUNICATING) {
                System.out.println("Currently no communication channel");
                return;
            }
            threadCopy = communicationThread;
        }
        // Perform the write unsynchronized
        threadCopy.write(out);
    }

    /**
     * Set the current state of service
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        this.serviceState = state;

        // Give the new state to the Handler so the UI Activity can update
        syncMessage(MESSAGE_STATE_CHANGE, String.valueOf(this.serviceState));
    }

    public synchronized int getState() {
        return this.serviceState;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tempServerSocket = null;
            // Create a new listening server socket
            try {
                tempServerSocket = bluetoothAdapter.
                        listenUsingRfcommWithServiceRecord(SERVICE_NAME,
                                MY_UUID);
            } catch (IOException e) {
                System.out.println("Socket listening failed");
            }
            serverSocket = tempServerSocket;
        }

        public void run() {
            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (serviceState != STATE_COMMUNICATING) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                System.out.println("get a connection request");
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (serviceState) {
                            case STATE_LISTENING:
                            case STATE_CONNECTING:
                                // Situation normal. Start communication.
                                System.out.println("about to start communication!");
                                startCommunication(socket,
                                        socket.getRemoteDevice());
                                break;
                            case STATE_IDLE:
                            case STATE_COMMUNICATING:
                                // Either not ready or already connected.
                                // Terminate new socket.
                                System.out.println("communication started");
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    System.out.println("Could not close unwanted socket");
                                }
                                break;
                        }
                    }
                }
            }
            System.out.println("END mAcceptThread, socket Type: ");
        }

        public void terminate() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Socket close failed");
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tempSocket = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tempSocket = device.createRfcommSocketToServiceRecord(
                        MY_UUID);
            } catch (IOException e) {
                System.out.println("Socket create failed");
            }
            socket = tempSocket;
        }

        public void run() {
            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                System.out.println("trying to connect");
                socket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    socket.close();
                } catch (IOException e2) {
                    System.out.println("unable to close socket " +
                            "during connection failure");
                }
                System.out.println("failted to get the socket");
                // send a message notify main thread of the failure
                syncMessage(MESSAGE_CONNECTION_FAILED, null);
                // restart listening mode
                startListening();
                return;
            }
            System.out.println("get the socket");
            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                connectThread = null;
            }
            // Start the connected thread
            startCommunication(socket, device);
        }

        public void terminate() {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("close of connect socket failed");
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class CommunicationThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public CommunicationThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tempInputStream = null;
            OutputStream tempOutputStream = null;

            // Get the BluetoothSocket input and output streams
            try {
                tempInputStream = socket.getInputStream();
                tempOutputStream = socket.getOutputStream();
            } catch (IOException e) {
                System.out.println("temp sockets not created");
            }
            inputStream = tempInputStream;
            outputStream = tempOutputStream;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);

                    String text = new String(Arrays.copyOf(buffer, bytes),
                            "UTF-8");
                    System.out.println("received message" + text);
                    // Send the obtained bytes to the UI Activity
                    syncMessage(MESSAGE_READ, text);
                } catch (IOException e) {
                    // Send a failure message back to the Activity
                    syncMessage(MESSAGE_CONNECTION_LOST, null);
                    // restart listening mode
                    startListening();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);

                // Echo the sent message back to the UI Activity
                syncMessage(MESSAGE_WRITE,
                        new String(buffer, "UTF-8"));
            } catch (IOException e) {
                System.out.println("Exception during write");
            }
        }

        public void terminate() {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("close() of connect socket failed");
            }
        }
    }
}