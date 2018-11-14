package com.bpt.tipi.streaming.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bpt.tipi.streaming.ServiceHelper;

import java.util.Timer;
import java.util.TimerTask;

public class CheckPeriodicalService extends Service {

    private static final String TAG = CheckPeriodicalService.class.getSimpleName();
    private Timer timer = new Timer();

    public CheckPeriodicalService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "-- onCreate");
        timer.scheduleAtFixedRate(new MainTask(), 0, 1000*5); //5 seg
    }

    @Override
    public ComponentName startService(Intent service) {
        Log.i(TAG, "-- startService");
        //timer.scheduleAtFixedRate(new MainTask(), 0, 5000); //5 seg
        return super.startService(service);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                //Verificar si los servicios están abajo y subirlos
                if (!isUsbConnected()) {
                    //Toast.makeText(getBaseContext(), ".:START ALL Services:.", Toast.LENGTH_SHORT).show(); //Debug
                    ServiceHelper.startAllServices(getBaseContext());
                }

            } catch (Exception e) {
                Log.e(TAG, "-- Error: " + e.getMessage());
            }
        }
    };



    private class MainTask extends TimerTask {
        public void run() {
            timerHandler.sendEmptyMessage(0);
        }
    }

    public  boolean isUsbConnected() {
        Intent intent = getBaseContext().registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        return intent.getExtras().getBoolean("connected");
    }
}
