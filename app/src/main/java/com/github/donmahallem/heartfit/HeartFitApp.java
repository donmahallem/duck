package com.github.donmahallem.heartfit;

import android.app.Application;

import timber.log.Timber;

public class HeartFitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
