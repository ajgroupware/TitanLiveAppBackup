package com.bpt.tipi.streaming;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.bpt.tipi.streaming.activity.MainActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class UnCaughtException implements Thread.UncaughtExceptionHandler {

    private Context context;

    public UnCaughtException(Context context) {
        this.context = context;
    }

    public void uncaughtException(Thread t, Throwable e) {
        File file = new File(Environment.getExternalStorageDirectory(), "logError.txt");
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file, true));
            e.printStackTrace(out);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApp.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) MyApp.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        //activity.finish();
        System.exit(2);
    }
}
