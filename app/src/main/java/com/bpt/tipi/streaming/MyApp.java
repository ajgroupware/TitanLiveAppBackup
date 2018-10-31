package com.bpt.tipi.streaming;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {

    public static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static MyApp getInstance() {
        return instance;
    }
}
