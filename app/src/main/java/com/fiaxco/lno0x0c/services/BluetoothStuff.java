package com.fiaxco.lno0x0c.services;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fiaxco.lno0x0c.R;


public class BluetoothStuff extends Activity {

    public final int BT_ENABLE_REQUEST_CODE = 1200;
    public BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    public BluetoothStuff(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void bluetoothSetup() {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) mContext).startActivityForResult(btEnableIntent, BT_ENABLE_REQUEST_CODE);
                ((Activity) mContext).invalidateOptionsMenu();
            }
        } else
            makeToast("Bluetooth not supported");
    }


    // Broadcast Receiver to observe Bluetooth ON OFF state
    public final BroadcastReceiver broadcastReceiverBTState = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        ((Activity) mContext).invalidateOptionsMenu();
                        makeToast("Bluetooth is required");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        ((Activity) mContext).invalidateOptionsMenu();
                        makeToast("Bluetooth Enabled");
                        break;
                }
            }
        }
    };


    private void makeToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }
}
