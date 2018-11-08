package com.bpt.tipi.streaming.receiver.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownReceiver extends BroadcastReceiver {

    private static final String TAG = ShutdownReceiver.class.getSimpleName();

    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    private static final String QUICKBOOT_POWEROFF = "android.intent.action.QUICKBOOT_POWEROFF";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_SHUTDOWN.equals(action) || QUICKBOOT_POWEROFF.equals(action)) {
            CameraEventHandler.appendEventLog(context, "DISPOSITIVO_APAGADO");
        }
    }
}
