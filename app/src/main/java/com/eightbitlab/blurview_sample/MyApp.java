package com.eightbitlab.blurview_sample;

import android.app.Application;

import com.eightbitlab.blurview_sample.net.AppGlobals;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppGlobals.init(this);
    }
}
