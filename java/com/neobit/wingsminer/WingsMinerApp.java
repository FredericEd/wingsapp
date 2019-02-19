package com.neobit.wingsminer;

import android.app.Application;
import androidx.work.Configuration;

import androidx.work.WorkManager;

public class WingsMinerApp extends Application {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        WorkManager.initialize(this, new Configuration.Builder().build());
        // Required initialization logic here!
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}