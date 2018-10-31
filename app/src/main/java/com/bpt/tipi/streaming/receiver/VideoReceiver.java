package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import com.bpt.tipi.streaming.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;

public class VideoReceiver extends BroadcastReceiver {

    EventBus bus = EventBus.getDefault();
    public static boolean firstConnect = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (firstConnect) {
            firstConnect = false;
            new CountDownTimer(500, 500) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    MessageEvent event = new MessageEvent(MessageEvent.LOCAL_RECORD);
                    bus.post(event);
                    firstConnect = true;
                }
            }.start();
        }
    }
}