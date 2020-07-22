package com.fiaxco.lno0x0c.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fiaxco.lno0x0c.CatalogActivity;
import com.fiaxco.lno0x0c.RecordActivity;
import com.fiaxco.lno0x0c.roomstuff.Profile;
import com.fiaxco.lno0x0c.roomstuff.ProfileContract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class BluetoothService extends Service implements LnoHandler.MessageConstants {


    public interface BluetoothServiceState {
        int STATE_NONE = 1000;
        int STATE_CONNECTING = 1002;
        int STATE_CONNECTED = 1003;
    }

    public int mState;
    public boolean mDownloadState = false;
    private Profile mCurrentProfile;


    BluetoothStuff mBluetoothStuff;
    BluetoothAdapter mBluetoothAdapter;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    public BluetoothSocket mSocket;
    public FileOutputStream mRecordFile;

    @Override
    public void onCreate() {
        super.onCreate();

        mBluetoothStuff = new BluetoothStuff(this);
        mBluetoothAdapter = mBluetoothStuff.mBluetoothAdapter;
        mState = BluetoothServiceState.STATE_NONE;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ContentValues values =
                intent.getParcelableExtra(RecordActivity.BT_SERVICE_INTENT_PROFILE_VALUE_EXTRA);

        assert values != null;
        mCurrentProfile = CatalogActivity.createProfileFromValues(values);
        mCurrentProfile.mId = values.getAsInteger(ProfileContract.ProfileEntry._ID);
        Log.d("ID", "onStartCommand: " + mCurrentProfile.mId);
        return START_NOT_STICKY;
    }




    // Bluetooth Service binder
    public class LocalBTServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }
    private final IBinder mBTServiceBinder = new LocalBTServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBTServiceBinder;
    }


    //----------------------------------------------------------------------------------------------- ConnectThread

    private class ConnectThread extends Thread {

        private static final String TAG = "BT Connect Thread";
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tempSoc = null;

            try {
                tempSoc = device.createRfcommSocketToServiceRecord(mBluetoothStuff.mUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Socket create method failed", e);
            }
            mmSocket = tempSoc;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();

            } catch (IOException e) {
                Log.e(TAG, "run: Socket connect failed lol", e);
                //cancel();
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            mSocket = mmSocket;
            connected(mmSocket, mmDevice);

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Socket close failed", e);
            }
            //connectionFailed();
        }
    }

    //----------------------------------------------------------------------------------------------- ConnectEDThread

    private class ConnectedThread extends Thread {

        private static final String TAG = "ConnectedThread";

        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;
        byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
                connectionLost();
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                connectionLost();
            }

            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;
        }

        @Override
        public void run() {
            mmBuffer = new byte[10240];
            int numBytes;

            try {
                Log.d(TAG, "run: filename" + getProfileFilename() );
//                FileOutputStream f = new FileOutputStream(new File("/sdcard/","foo.wav"));

                int totalBytes = 0;
                while ((numBytes = mmInputStream.read(mmBuffer)) != -1) {
                    Log.d(TAG, "run: LOLOLOLOL");
                    totalBytes += numBytes;
                    mRecordFile.write(mmBuffer, 0, numBytes);
                }
                mRecordFile.close();
                Log.d("fff", "FILE run: total bytes: " + totalBytes);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "FILE run: Fail");
                connectionLost();
            }

//            while (true) {
//                try {
//                    numBytes = mmInputStream.read(mmBuffer, 0, 10240);
//
//                    totalBytes += numBytes;
//                    Log.d(TAG, "run total size : " + totalBytes);
//                    Log.d(TAG, "bt soc read num bytes: " + numBytes);
//                    String msg =  new String(mmBuffer, StandardCharsets.UTF_8);
//                    Log.d(TAG, "bt soc read: " + msg);
//
//                } catch (IOException e) {
//                    Log.d(TAG, "Input stream was disconnected", e);
//                    connectionLost();
//                    BluetoothService.this.stop();
//                    break;
//                }
//            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutputStream.write(bytes);

            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                connectionLost();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }




    //----------------------------------------------------------------------------------------------- connection Helpers

    public void connectPnoi() {
        setState(BluetoothServiceState.STATE_CONNECTING);
        BluetoothDevice btDevice = getBtDevicesWithName("naomi");
        if (btDevice != null) {
            connectToDevice(btDevice);
        }
    }

    private synchronized void connectToDevice(BluetoothDevice device) {
        if (mState == BluetoothServiceState.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

        setState(BluetoothServiceState.STATE_CONNECTING);
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(BluetoothServiceState.STATE_CONNECTED);
    }

    public void sendCommand(String command) {
        if (mConnectedThread != null) {
            mConnectedThread.write(command.getBytes());
        } else {
            mBluetoothStuff.makeToast("Pnoi not connected");
        }
    }

    public void receiveData() {
        if (mConnectedThread != null) {
            //mConnectedThread.resetInputStream();
            boolean f = false;
            try {
                mRecordFile = new FileOutputStream(new File("/sdcard/",getProfileFilename()));
                Log.d("fff", "receiveData: " + getProfileFilename());
                f = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if(f) sendCommand("download");
        }

    }


    private void connectionFailed() {
        BluetoothService.this.stop();
    }

    private void connectionLost() {
        setState(BluetoothServiceState.STATE_NONE);
        Intent disconnectBCIntent = new Intent(RecordActivity.BT_CONN_STATE_BROADCAST);
        disconnectBCIntent.putExtra("disconnect", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(disconnectBCIntent);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }




    //----------------------------------------------------------------------------------------------- service helpers


    // get bluetooth device with given name
    private BluetoothDevice getBtDevicesWithName(String name) {

        Set<BluetoothDevice> btDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice btDevice : btDevices) {
            String devName = btDevice.getName();
            if (devName.equals(name)) {
                return btDevice;
            }
        }
        mBluetoothStuff.makeToast("No device named " + name + " is paired");
        return null;
    }

    private String getProfileFilename() {
        SimpleDateFormat d = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH);
        String time = d.format(new Date());

        return mCurrentProfile.mId + "__" +
                time + "__" + mCurrentProfile.mName.replace(" ", "-") + "_" +
                mCurrentProfile.mAge + "_" +
                ProfileContract.ProfileEntry.genderType(mCurrentProfile.mGender) + "_" +
                mCurrentProfile.mHeight + "_" +
                mCurrentProfile.mWeight + "_" +
                RecordActivity.mLocation.replace(" ", "") + ".wav";
    }

    // change the state of the bluetooth service and send a broadcast received by record activity
    private void setState(int state) {
        this.mState = state;
        Intent btConBroadcast = new Intent(RecordActivity.BT_CONN_STATE_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(btConBroadcast);
    }

    public synchronized void stop() {
        setState(BluetoothServiceState.STATE_NONE);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        stopSelf();
    }

    @Override
    public boolean stopService(Intent name) {
        setState(BluetoothServiceState.STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        stop();
        mBluetoothStuff.makeToast("Pnoi disconnected");
        super.onDestroy();
    }
}
