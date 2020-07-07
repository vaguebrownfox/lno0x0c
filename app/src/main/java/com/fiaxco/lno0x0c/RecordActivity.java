package com.fiaxco.lno0x0c;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fiaxco.lno0x0c.roomstuff.ProfileContract.ProfileEntry;
import com.fiaxco.lno0x0c.roomstuff.ProfileContract.RecordEntry;
import com.fiaxco.lno0x0c.services.BluetoothStuff;
import com.fiaxco.lno0x0c.services.LnoHandler;
import com.fiaxco.lno0x0c.services.TimerService;

public class RecordActivity extends AppCompatActivity implements LnoHandler.MessageConstants {

    private ContentValues mProfileValues;

    Button mLUL, mRUL, mLLL, mRLL;
    private byte[] mCurrentLocation;
    private String mLocation;
    private TextView mRecordLocText;
    private TextView mFileNameText;

    Button mRecordButton;
    private BluetoothStuff mBluetoothStuff;

    private LnoHandler.TimerHandler mTimerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // Class to handle bluetooth setup
        mBluetoothStuff = new BluetoothStuff(this);
        // Intent filter for Bluetooth on/off status broadcast receiver
        IntentFilter btStateBroadcast = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStuff.broadcastReceiverBTState, btStateBroadcast);

        // Receive intent from editor activity
        Intent recordIntent = getIntent();
        mProfileValues =
                recordIntent.getParcelableExtra(EditorActivity.EDITOR_ACTIVITY_PROFILE_VALUE_EXTRA);

        // Location select buttons
        setupLocButtons();

        // Class to handle timer
        TimerService mTimer = new TimerService();
        //Handler class to start/stop timer
        mTimerHandler = new LnoHandler.TimerHandler(this, mTimer);

        // Record start/stop/done
        mRecordButton = findViewById(R.id.button_record_start);
        mRecordButton.setOnClickListener(v -> {
            if (!mTimer.running) { // TODO: && !isRecordDone (global var to track record finish
                mTimerHandler.sendEmptyMessage(MSG_START_TIMER);
                mRecordButton.setText(R.string.rec_activity_button_text_stop);
                locationButtonVisibilityHelper(true);
                mFileNameText.setText(mLocation);
            } else {
                mTimerHandler.sendEmptyMessage(MSG_STOP_TIMER);
                mRecordButton.setText(R.string.record_activity_rec_button_record);
                locationButtonVisibilityHelper(false);
            }
        });

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
            } else
                btIcon.setIcon(R.drawable.bluetooth_24);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_record_bluetooth:
                mBluetoothStuff.bluetoothSetup();
                return true;

            case R.id.action_record_download_record:
                makeToast("download");
                return true;

            case R.id.action_record_connect_pnoi:
                makeToast("pnoi bt socket");
                return true;

            case R.id.action_record_new_record:
                makeToast("new");
                return true;

            case R.id.action_record_bluetooth_disconnect:
                makeToast("disconnect");
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

    // Toast in record context
    private void makeToast(String msg) {
        Toast.makeText(RecordActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /*-------------- /helper methods ---------------*/

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBluetoothStuff.broadcastReceiverBTState);
    }

}