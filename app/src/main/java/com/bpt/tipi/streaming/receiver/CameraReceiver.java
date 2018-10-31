package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

public class CameraReceiver extends BroadcastReceiver {

    public static boolean firstConnect = true;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (firstConnect) {
            firstConnect = false;
            new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    //Toast.makeText(context, "Opción disponible proximamente", Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent();
                    //intent.setClassName("com.android.gallery3d", "com.android.camera.CameraActivity");
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //context.startActivity(intent);
                    firstConnect = true;
                }
            }.start();
        }
    }
}
