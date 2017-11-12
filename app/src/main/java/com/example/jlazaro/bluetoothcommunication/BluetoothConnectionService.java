package com.example.jlazaro.bluetoothcommunication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by J.Lazaro on 11/11/2017.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private final BluetoothAdapter mBluetoothAdapter;

    Context mContext;

    public BluetoothConnectionService(Context context) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = context;
    }



    //thread that runs while listening for incoming connections
    //it behaves like a server-side client
    //it runs until a connection is accepted (or until cancelled)
    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            //create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: "+  e.getMessage());
            }
            mmServerSocket = tmp;
        }
        
        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;
            try {
                //This is a blocking call and will olnly return on a
                //successfull connection or an exception
                Log.d(TAG, "run: RFCOM server socet start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection");

            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: "+  e.getMessage());
            }

            //In Third Tutorial
            if(socket != null){
                connected(socket,mmDevice);
            }

            Log.i(TAG, "run: END mAcceptThread");
        }
        
        public void cancel(){
            Log.d(TAG, "cancel: Canceling AcceptThread");
            try{
                mmServerSocket.close();
            } catch(IOException e){
                Log.e(TAG, "cancel: Close AcceptThread ServerSocket failed. " +e.getMessage() );
            }

        }
    }

    public class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "run: mConnectThread");

            //Get a BluetoothSocket for a connection with the
            //given BluetoothDevice
            try {
                Log.d(TAG, "run: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "run: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();

            try {
                //This is a blocking call and will olnly return on a
                //successfull connection or an exception
                mmSocket.connect();
                Log.d(TAG, "run: Connection Successful");
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket");
                } catch (IOException e1) {
                    Log.e(TAG, "run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: Could not connect to UUID: " + MY_UUID_INSECURE);
            }
            //In third Tutorial
            connected(mmSocket,mmDevice);
        }

        public void cancel(){
            Log.d(TAG, "cancel: Closing Client Socket");
            try{
                mmSocket.close();
            } catch(IOException e){
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread " + e.getMessage() );
            }
        }
    }

    //start the chat service.
    //Start the AcceptThread to begin a session in listening(server) mode.
    //Called by the Activity onResume()
    public synchronized void start(){
        Log.d(TAG, "start");

        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    //AcceptThread starts and waits for a connection
    //Then ConnectThread starts and tries to make a connection with the other devices AcceptThread
    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started");

        //Initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }
}
