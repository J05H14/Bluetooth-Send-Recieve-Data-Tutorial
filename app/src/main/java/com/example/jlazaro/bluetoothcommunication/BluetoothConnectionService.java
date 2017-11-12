package com.example.jlazaro.bluetoothcommunication;

import android.bluetooth.BluetoothAdapter;
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
}
