package com.ksyun.mc.AgoraARTCDemo.utils;

import android.app.Application;

/**
 * Created by xiaoqiang on 2017/3/31.
 */

public class CrashApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }
}
