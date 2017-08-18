package com.ksyun.mc.AgoraARTCDemo.kit;

import android.content.Context;

/**
 * Created by xiaoqiang on 2017/8/16.
 */

public interface KMCStreamer {
    String TAG = KMCStreamer.class.getName();
    void initStream(String roomName, Context context,OnStateListener listener);
    void destroyStream();
//    void startStream();
//    void stopStream();
    void startRTC(String tempChannel);
    void stopRTC();
    void onResume();
    void onPause();
    boolean isPlayering();
    boolean isRCT();
    interface OnStateListener{
        void onSuccess();
        void onFailed(String msg);
        void onRTCSuccess();
        void onRTCFailed(String msg);
    }
}
