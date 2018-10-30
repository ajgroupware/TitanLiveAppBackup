package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.util.Log;

import com.bpt.tipi.streaming.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;

public class ConnectivityReceiver extends BroadcastReceiver {

    EventBus bus = EventBus.getDefault();
    public static boolean firstConnect = true;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("Depuracion", "ConnectivityReceiver ");
        if (firstConnect) {
            firstConnect = false;
            new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    Log.i("Depuracion", "isConnected " + haveNetworkConnection(context));
                    if (haveNetworkConnection(context)) {
                        MessageEvent event = new MessageEvent(MessageEvent.RECONNECT);
                        bus.post(event);
                    }
                    firstConnect = true;
                }
            }.start();
        }
    }

    private boolean haveNetworkConnection(Context context) {
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.isConnected())
                isConnected = true;
        }
        return isConnected;
    }
}
