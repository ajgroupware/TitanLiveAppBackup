package com.bpt.tipi.streaming.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jpujolji on 12/05/17.
 */

public class VideoNameHelper {

    public static File getOutputFile(Context context, int secuence) {
        return new File(getMediaFolder(), getNameFile(context, secuence) + ".mp4");
    }

    private static String getNameFile(Context context, int secuence) {
        String nameVideo;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        if (secuence == 1) {
            String deviceId = PreferencesHelper.getDeviceId(context);
            if (deviceId.isEmpty()) {
                nameVideo = "titanLive_" + timeStamp;
            } else {
                nameVideo = deviceId + "_" + timeStamp;
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("name_video_local", nameVideo);
            editor.apply();
        } else {
            nameVideo = preferences.getString("name_video_local", "") + "_" + secuence;
            if (secuence == 2) {
                renameVideo(nameVideo);
            }
        }
        return nameVideo;
    }

    public static String getCurrentNameFile(Context context) {
        String nameVideo;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        nameVideo = preferences.getString("name_video_local", "");
        return nameVideo;
    }

    private static void renameVideo(String name) {
        File oldFile, newFile;
        oldFile = new File(getMediaFolder(), name + ".mp4");
        newFile = new File(getMediaFolder(), name + "_1.mp4");
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
        }
    }

    public static File getMediaFolder() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory("DCIM"), "100MEDIA");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i("Depuracion", "Error al crear el directorio");
                return null;
            }
        }
        return mediaStorageDir;
    }

    public static void taggedVideo(String name, int label) {
        File[] dirs = getMediaFolder().listFiles();
        try {
            for (File file : dirs) {
                String fileName;
                if (name.contains(".")) {
                    fileName = name.substring(0, name.lastIndexOf("."));
                } else {
                    fileName = name;
                }
                Log.i("Depuracion", "fileName " + file.getName() + " " + fileName);
                if (file.getName().contains(fileName)) {
                    File newFile = new File(getMediaFolder(), label + "_" + file.getName());
                    file.renameTo(newFile);
                }
            }
        } catch (Exception e) {
            Log.i("Depuracion", "error " + e);
            e.fillInStackTrace();
        }
    }

    public static File getNamePhoto(Context context) {
        String nameVideo;
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String deviceId = PreferencesHelper.getDeviceId(context);
        if (deviceId.isEmpty()) {
            nameVideo = "titanLive_" + timeStamp;
        } else {
            nameVideo = deviceId + "_" + timeStamp;
        }
        return new File(getMediaFolder(), nameVideo + ".jpg");
    }


}
