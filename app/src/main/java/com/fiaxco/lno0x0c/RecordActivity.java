package com.fiaxco.lno0x0c;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fiaxco.lno0x0c.roomstuff.ProfileContract.ProfileEntry;
import com.fiaxco.lno0x0c.roomstuff.ProfileContract.RecordEntry;
import com.fiaxco.lno0x0c.services.BluetoothService;
import com.fiaxco.lno0x0c.services.BluetoothStuff;
import com.fiaxco.lno0x0c.services.LnoHandler;
import com.fiaxco.lno0x0c.services.TimerService;

public class RecordActivity extends AppCompatActivity
        implements LnoHandler.MessageConstants, BluetoothService.BluetoothServiceState {


    public static final String BT_CONN_STATE_BROADCAST = BuildConfig.APPLICATION_ID + ".btconnbroadcast";
    public static final String BT_SERVICE_INTENT_PROFILE_VALUE_EXTRA = BuildConfig.APPLICATION_ID + ".beneficial";
    private ContentValues mProfileValues;

    Button mLUL, mRUL, mLLL, mRLL;
    private byte[] mCurrentLocation;
    public static String mLocation = "";
    private TextView mRecordLocText;
    private TextView mFileNameText;

    Button mRecordButton;
    public BluetoothStuff mBluetoothStuff;
    public boolean mRecordDone = false;

    private LnoHandler.TimerHandler mTimerHandler;

    BluetoothService mBluetoothService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // Class to handle bluetooth setup
        mBluetoothStuff = new BluetoothStuff(this);

        // Intent filter for Bluetooth on/off status broadcast receiver
        {
            IntentFilter btStateBroadcast = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBluetoothStuff.broadcastReceiverBTState, btStateBroadcast);
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(btConnectionStateBroadcast,
                            new IntentFilter(BT_CONN_STATE_BROADCAST));
        }

        // Receive intent from editor activity
        {
            Intent recordIntent = getIntent();
            mProfileValues =
                    recordIntent.getParcelableExtra(EditorActivity.EDITOR_ACTIVITY_PROFILE_VALUE_EXTRA);
        }

        Intent intent = new Intent(this, BluetoothService.class);
        intent.putExtra(BT_SERVICE_INTENT_PROFILE_VALUE_EXTRA, mProfileValues);
        startService(intent);

        // Location select buttons
        setupLocButtons();

        // Class to handle timer
        TimerService mTimer = new TimerService();

        //Handler class to start/stop timer
        mTimerHandler = new LnoHandler.TimerHandler(this, mTimer);

        // Record start/stop/done
        mRecordButton = findViewById(R.id.button_record_start);
        mRecordButton.setOnClickListener(v -> {
            if (mBound) {
                if (mBluetoothService.mState == STATE_CONNECTED) {
                    if (!mTimer.running && !mRecordDone) { // !isRecordDone (global var to track record finish)
                        recordButtonStartHelper();
                    } else {
                        if (!mRecordDone)
                            recordButtonStopHelper();
                        else
                            recordButtonDoneHelper();
                    }
                }
            } else {
                makeToast("Pnoi not connected");
            }

        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, btServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(btServiceConnection);
            mBound = false;
        }
    }

    /*-------------- Menu stuff ---------------*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem btIcon = menu.findItem(R.id.action_record_bluetooth);
        if (btIcon != null) {

            if (mBluetoothStuff.mBluetoothAdapter.isEnabled()) {
                btIcon.setIcon(R.drawable.bluetooth_on_24);
                if (mBluetoothService != null) {
                    if (mBluetoothService.mState == STATE_CONNECTING) {
                        btIcon.setIcon(R.drawable.bluetooth_searching_24);
                    }
                    if (mBluetoothService.mState == STATE_CONNECTED) {
                        btIcon.setIcon(R.drawable.bluetooth_connected_24);
                        makeToast("Pnoi connected");
                    }
                }
            }
        } else
            makeToast("Bluetooth service not started");
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_record_bluetooth:   // bluetooth on
                mBluetoothStuff.bluetoothSetup();
                return true;

            case R.id.action_record_download_record: // download
                if (mBound)
                    if (mBluetoothService.mState == STATE_CONNECTED)
                        //mBluetoothService.sendCommand("download");
                        mBluetoothService.receiveData();
                        Log.d("downloadbt", "onOptionsItemSelected: pressed");
                return true;

            case R.id.action_record_connect_pnoi:  // connect
                if (mBound) {
                    if (mBluetoothService.mState != STATE_CONNECTED)
                        mBluetoothService.connectPnoi();
                    else
                        makeToast("Pnoi already connected");
                }
                return true;

            case R.id.action_record_new_record:   // reset record
                makeToast("new");
                return true;

            case R.id.action_record_bluetooth_disconnect:   // close bluetooth service
                // if (mBound) mBluetoothService.sendCommand("stop");
                recordButtonStopHelper();
                Intent intent = new Intent(this, BluetoothService.class);
                stopService(intent);
                if (mBound) unbindService(btServiceConnection);
                mBound = false;
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*-------------- /Menu stuff ---------------*/




    /*-------------- Location selection and Record button methods ---------------*/

    public void onClickLUL(View view) {
        mCurrentLocation = new byte[] {1, 0, 0, 0};
        locationButtonSelectHelper(mCurrentLocation, RecordEntry.LUL);
    }

    public void onClickRUL(View view) {
        mCurrentLocation = new byte[] {0, 1, 0, 0};
        locationButtonSelectHelper(mCurrentLocation, RecordEntry.RUL);
    }

    public void onClickLLL(View view) {
        mCurrentLocation = new byte[] {0, 0, 1, 0};
        locationButtonSelectHelper(mCurrentLocation, RecordEntry.LLL);
    }

    public void onClickRLL(View view) {
        mCurrentLocation = new byte[] {0, 0, 0, 1};
        locationButtonSelectHelper(mCurrentLocation, RecordEntry.RLL);
    }


    /*-------------- /Location selection methods ---------------*/





    /*-------------- helper methods ---------------*/



    private void setupLocButtons() {
        assert mProfileValues != null;
        setTitle(mProfileValues.getAsString(ProfileEntry.NAME));

        mLUL = findViewById(R.id.button_left_upper_lung);
        mRUL = findViewById(R.id.button_right_upper_lung);
        mLLL = findViewById(R.id.button_left_lower_lung);
        mRLL = findViewById(R.id.button_right_lower_lung);
        mRecordLocText = findViewById(R.id.text_view_location);
        mFileNameText = findViewById(R.id.text_view_record_file);
        mCurrentLocation = new byte[] { 1, 0, 0, 0 };
        locationButtonSelectHelper(mCurrentLocation, RecordEntry.LUL);

    }
    private void locationButtonSelectHelper(byte[] buttonLoc, String loc) {
        mLocation = loc;
        int[] mLocButtonColors = {
                getResources().getColor(R.color.colorUnselected),
                getResources().getColor(R.color.colorAccent)
        };
        mLUL.setBackgroundTintList(ColorStateList.valueOf(mLocButtonColors[buttonLoc[0]]));
        mRUL.setBackgroundTintList(ColorStateList.valueOf(mLocButtonColors[buttonLoc[1]]));
        mLLL.setBackgroundTintList(ColorStateList.valueOf(mLocButtonColors[buttonLoc[2]]));
        mRLL.setBackgroundTintList(ColorStateList.valueOf(mLocButtonColors[buttonLoc[3]]));

        mRecordLocText.setText(mLocation);
    }
    private void locationButtonVisibilityHelper(boolean isStart) {
        final int[] mVisibility = new int[] {
                View.INVISIBLE,
                View.VISIBLE
        };
        mVisibility[0] = isStart ? View.INVISIBLE : View.VISIBLE;
        mLUL.setVisibility(mVisibility[mCurrentLocation[0]]);
        mRUL.setVisibility(mVisibility[mCurrentLocation[1]]);
        mLLL.setVisibility(mVisibility[mCurrentLocation[2]]);
        mRLL.setVisibility(mVisibility[mCurrentLocation[3]]);
    }



    // Record button control flow helpers
    private void recordButtonStartHelper() {
        mBluetoothService.sendCommand("record");
        mTimerHandler.sendEmptyMessage(MSG_START_TIMER);
        mRecordButton.setText(R.string.rec_activity_button_text_stop);
        locationButtonVisibilityHelper(true);
        mFileNameText.setText(mLocation);
    }

    private void recordButtonStopHelper() {
        mBluetoothService.sendCommand("stop");
        mTimerHandler.sendEmptyMessage(MSG_STOP_TIMER);
        mRecordButton.setText(R.string.record_activity_record_done);
        mRecordButton.setBackgroundTintList(ColorStateList
                .valueOf(getResources().getColor(R.color.colorDoneGreen)));
        mRecordDone = true;
    }

    private void recordButtonDoneHelper() {
        mBluetoothService.sendCommand("done");
        locationButtonVisibilityHelper(false);
        mTimerHandler.sendEmptyMessage(MSG_RESET_TIMER);
        mRecordButton.setText(R.string.record_activity_rec_button_record);
        mRecordButton.setBackgroundTintList(ColorStateList
                .valueOf(getResources().getColor(R.color.colorAccent)));
        mRecordDone = false;
    }

    // Toast in record context
    private void makeToast(String msg) {
        Toast.makeText(RecordActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /*-------------- /helper methods ---------------*/





    /*-------------- BT service connection ---------------*/

    private ServiceConnection btServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBTServiceBinder btServiceBinder =
                    (BluetoothService.LocalBTServiceBinder) service;
            mBluetoothService = btServiceBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    /*-------------- /BT service connection ---------------*/



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStuff.broadcastReceiverBTState);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btConnectionStateBroadcast);
    }


    private final BroadcastReceiver btConnectionStateBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BT_CONN_STATE_BROADCAST.equals(action)) {
                invalidateOptionsMenu();
            }
        }
    };
}