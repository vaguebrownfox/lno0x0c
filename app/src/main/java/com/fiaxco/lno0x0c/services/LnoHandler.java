package com.fiaxco.lno0x0c.services;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fiaxco.lno0x0c.R;

public class LnoHandler {

    public interface MessageConstants {
        int MSG_START_TIMER = 6000;
        int MSG_UPDATE_TIMER = 6001;
        int MSG_STOP_TIMER = 6002;
    }

    // Record activity timer handler
    public static class TimerHandler extends Handler implements MessageConstants {

        Context mContext;
        TimerService mTimer;
        TextView mTextViewRecordTimer;
        public TimerHandler(Context context, TimerService timer) {
            mContext = context;
            mTimer = timer;
            mTextViewRecordTimer =
                    ((Activity) mContext).findViewById(R.id.text_view_record_timer);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int REFRESH_RATE = 1000;
            switch (msg.what) {
                case MSG_START_TIMER :
                    mTimer.start();
                    sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;
                case MSG_UPDATE_TIMER :
                    mTextViewRecordTimer.setText(mTimer.getElapsedTimeMinutes());
                    sendEmptyMessageDelayed(MSG_UPDATE_TIMER, REFRESH_RATE);
                    break;
                case MSG_STOP_TIMER :
                    removeMessages(MSG_UPDATE_TIMER);
                    mTimer.stop();
                    break;
                default:
                    break;
            }
        }
    }



}
