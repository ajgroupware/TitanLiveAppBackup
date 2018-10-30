package com.bpt.tipi.streaming.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.UnCaughtException;
import com.bpt.tipi.streaming.model.Device;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.network.HttpInterface;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

public class LocationService extends Service implements LocationListener, HttpInterface {
    LocationManager locationManager;
    Location mLocation;

    MyCounter myCounter;

    private EventBus bus = EventBus.getDefault();

    private final IBinder mBinder = new LocalBinder();

    private Handler mHandler;

    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Depuracion", "LocationService onCreate() ");
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(this));
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        HandlerThread thread = new HandlerThread("LocationService");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int interval = ConfigHelper.getIntervalLocation(LocationService.this);

        initVideoCounter(interval);
        startVideoCounter();
        bus.register(this);
        return START_STICKY;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(final MessageEvent event) {
        switch (event.key) {
            case MessageEvent.SOS_ENABLED:
                int interval;
                pauseVideoCounter();
                if (event.content.contains("true")) {
                    interval = ConfigHelper.getIntervalLocationInSos(LocationService.this);
                } else {
                    interval = ConfigHelper.getIntervalLocation(LocationService.this);
                }
                initVideoCounter(interval);
                startVideoCounter();
                break;
        }
    }

    public void initVideoCounter(final int interval) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myCounter = new LocationService.MyCounter(interval * 1000, 1000);
            }
        });
    }

    public void pauseVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myCounter.cancel();
            }
        });
    }

    public void startVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myCounter.start();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        myCounter.cancel();
        bus.unregister(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LocationService.this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.latitude), "" + location.getLatitude());
        editor.putString(getString(R.string.longitude), "" + location.getLongitude());
        editor.apply();
    }

    private class MyCounter extends CountDownTimer {

        MyCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            try {
                String idDevice = ConfigHelper.getDeviceName(LocationService.this);
                //if (mLocation != null && !idDevice.isEmpty()) {
                //double latitude = 6.2325358;//mLocation.getLatitude();//
                //double longitude = -75.6434221;//mLocation.getLongitude();//
                double latitude = mLocation.getLatitude();//
                double longitude = mLocation.getLongitude();//
                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                Device device = new Device(idDevice, "CONNECTED", latitude, longitude, batLevel);
                Gson gson = new Gson();
                String json = gson.toJson(device);

                HttpClient httpClient = new HttpClient(LocationService.this);
                httpClient.httpRequest(json, HttpHelper.Method.REPORT_STATUS, HttpHelper.TypeRequest.TYPE_POST, true);
                //} else {
                //    Log.i("Depuracion", "mLocation " + mLocation);
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
            start();
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSuccess(String method, JSONObject response) {
        Log.i("Depuracion", "onSuccess location " + response.toString());
    }

    @Override
    public void onFailed(String method, JSONObject errorResponse) {
        Log.i("Depuracion", "onFailed location " + errorResponse);
    }
}