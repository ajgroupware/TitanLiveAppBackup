package com.bpt.tipi.streaming.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.bpt.tipi.streaming.service.CameraService;
import com.bpt.tipi.streaming.service.RecorderService;
import com.bpt.tipi.streaming.service.LocationService;
import com.bpt.tipi.streaming.service.MqttMessageService;

public class ServiceHelper {

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static synchronized void startMqttService(Context context) {
        String idDevice = PreferencesHelper.getDeviceId(context);
        if (!idDevice.isEmpty()) {
            if (!isServiceRunning(context, MqttMessageService.class)) {
                context.startService(new Intent(context, MqttMessageService.class));
            }
        }
    }

    public static synchronized void stopMqttService(Context context) {
        if (isServiceRunning(context, MqttMessageService.class)) {
            context.stopService(new Intent(context, MqttMessageService.class));
        }
    }

    public static synchronized void stopCameraService(Context context) {
        if (isServiceRunning(context, CameraService.class)) {
            context.stopService(new Intent(context, CameraService.class));
        }
    }

    public static synchronized void startCameraService(Context context) {
        if (!isServiceRunning(context, CameraService.class)) {
            context.startService(new Intent(context, CameraService.class));
        }
    }

    public static synchronized void startLocationService(Context context) {
        String idDevice = PreferencesHelper.getDeviceId(context);
        if (!idDevice.isEmpty()) {
            if (!isServiceRunning(context, LocationService.class)) {
                context.startService(new Intent(context, LocationService.class));
            }
        }
    }

    public static synchronized void stopLocationService(Context context) {
        if (isServiceRunning(context, LocationService.class)) {
            context.stopService(new Intent(context, LocationService.class));
        }
    }

    public static void stopAllServices(Context context) {
        stopMqttService(context);
        stopCameraService(context);
        stopLocationService(context);
    }

    public static void startAllServices(Context context) {
        startMqttService(context);
        startCameraService(context);
        startLocationService(context);
    }

}