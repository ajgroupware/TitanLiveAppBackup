package com.bpt.tipi.streaming.receiver.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimChangeReceiver extends BroadcastReceiver {

    private static final String TAG = SimChangeReceiver.class.getSimpleName();

    private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";

    private static final String EXTRA_SIM_STATE = "ss";

    private static final String SIM_STATE_LOADED = "LOADED";
    private static final String SIM_STATE_ABSENT = "ABSENT";
    private static final String SIM_STATE_UNKNOWN = "UNKNOWN";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_SIM_STATE_CHANGED.equals(action)) {
            Bundle extras = intent.getExtras();
            String state = extras.getString(EXTRA_SIM_STATE);
            Log.d(TAG, "SIM Action : " + action + " / State : " + state);
            if (SIM_STATE_ABSENT.equals(state)) {
                //Sim card desactivada (retirada)
                CameraEventHandler.appendEventLog(context, "SIM_EXTRAIDA ABSENT");
            } else if (SIM_STATE_UNKNOWN.equals(state)) {
                //Sim card instalada
                CameraEventHandler.appendEventLog(context, "SIM_INSTALADA UNKNOWN");
            } else if (SIM_STATE_LOADED.equals(state)) {
                //Camára con DATOS
                CameraEventHandler.appendEventLog(context, "SIM_CONFIGURADA LOADED");
            }


        }
    }
}
