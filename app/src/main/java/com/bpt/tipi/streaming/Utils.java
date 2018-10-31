package com.bpt.tipi.streaming;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.bpt.tipi.streaming.helper.VideoNameHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jpujolji on 28/08/17.
 */

public class Utils {

    public static boolean isCameraExist(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Camera getCameraInstance(int cameraId) {
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            Log.d("TAG", "Open camera failed: " + e);
        }
        return camera;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void saveVideoLocation(Context context, String nameFile) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String latitude = preferences.getString(context.getString(R.string.latitude), "");
        String longitude = preferences.getString(context.getString(R.string.longitude), "");
        if (latitude.isEmpty() || longitude.isEmpty()) {
            return;
        }
        String currentPosition = latitude + "," + longitude;
        File file = new File(VideoNameHelper.getMediaFolder(), nameFile + ".dat");
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file, true));
            out.append(currentPosition).append("\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo para calcular el numero de videos pendientes por etiquetar
     *
     * @return numero de videos pendientes
     */
    public static int getNumberPending() {
        int count = 0;
        try {
            File[] dirs = VideoNameHelper.getMediaFolder().listFiles();
            for (File file : dirs) {
                if (isVideoAndNotLabel(file)) {
                    count = count + 1;
                }
            }
            return count;
        } catch (Exception e) {

        }
        return count;
    }

    public static boolean isVideoAndNotLabel(File file) {
        String filename = file.getName().toLowerCase();
        String extension = filename.substring(filename.lastIndexOf("."), filename.length());
        return extension.equals(".mp4") && !TextUtils.isDigitsOnly(filename.substring(0, 1));
    }

    public static String getImeiDevice(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static void updateWaterMark(Context context) {
        String idDevice = ConfigHelper.getDeviceName(context);
        File file = new File(Environment.getExternalStorageDirectory(), "gpsinfo.txt");
        if (file.exists()) {
            try {
                PrintWriter out = new PrintWriter(new FileWriter(file, false));
                out.print("TITAN-" + idDevice);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getIdLabel(int position) {
        switch (position) {
            case 1:
                return 88;
            case 2:
                return 90;
            case 3:
                return 47;
            case 4:
                return 13;
            case 5:
                return 91;
            case 6:
                return 58;
            case 7:
                return 18;
            case 8:
                return 22;
            case 9:
                return 23;
            case 10:
                return 24;
            case 11:
                return 69;
            case 12:
                return 70;
            case 13:
                return 29;
            case 14:
                return 104;
            case 15:
                return 105;
            case 16:
                return 84;
            case 17:
                return 43;
            default:
                return 1;
        }
    }

}