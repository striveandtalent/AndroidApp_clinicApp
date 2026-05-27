package com.eightbitlab.blurview_sample.net;

import android.content.Context;

public class AppGlobals {
    private static Context context;

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
