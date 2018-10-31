package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.bpt.tipi.streaming.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;

public class CameraReceiver extends BroadcastReceiver {

    public static boolean firstConnect = true;
    EventBus bus = EventBus.getDefault();

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (firstConnect) {
            firstConnect = false;
            new CountDownTimer(500, 500) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    MessageEvent event = new MessageEvent(MessageEvent.TAKE_PHOTO);
                    bus.post(event);
                    firstConnect = true;
                }
            }.start();
        }
    }
}
