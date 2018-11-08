package com.bpt.tipi.streaming.receiver.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bpt.tipi.streaming.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraEventHandler {

    private static final String TAG = CameraEventHandler.class.getSimpleName();


    public static void appendEventLog(Context context, String event) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        File logStorageDir = new File(Environment.getExternalStoragePublicDirectory("DCIM"), "LOG");
        if (!logStorageDir.exists()) {
            if (!logStorageDir.mkdirs()) {
                Log.i("Depuracion", "Error al crear el directorio");
            }
        }

        File eventLogFile = new File(logStorageDir, "TitanLive_eventLog.txt");
        if (!eventLogFile.exists()) {
            try {
                eventLogFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        try {
            String deviceId = preferences.getString(context.getString(R.string.id_device), "NONE");
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(eventLogFile, true));
            buf.append(logEventText(event, deviceId));
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String logEventText(String event, String deviceId) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StringBuilder log = new StringBuilder(df.format(new Date()));
        log.append("\t");
        //log.append(deviceId);
        //log.append("\t");
        log.append(event);
        Log.d(TAG, "--log event : " + log.toString());
        return log.toString();
    }

}
