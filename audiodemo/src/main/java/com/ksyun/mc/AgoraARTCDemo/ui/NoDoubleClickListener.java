package com.ksyun.mc.AgoraARTCDemo.ui;

import android.util.Log;
import android.view.View;

import java.util.Calendar;

/**
 * 防止重复点击
 */

public abstract class NoDoubleClickListener implements View.OnClickListener {
    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleClick(v);
        }else{
            Log.w(NoDoubleClickListener.class.getName(),"不可以重复快速点击按钮");
        }
    }

    public abstract void onNoDoubleClick(View v);
}
