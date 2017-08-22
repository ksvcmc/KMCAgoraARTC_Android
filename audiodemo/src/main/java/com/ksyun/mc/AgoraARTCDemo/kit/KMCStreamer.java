package com.ksyun.mc.AgoraARTCDemo.kit;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by xiaoqiang on 2017/8/16.
 */

public interface KMCStreamer {
    String TAG = KMCStreamer.class.getName();
    void initStream(@NonNull String roomName, @NonNull Context context,@NonNull OnStateListener listener);
    void destroyStream();
//    void startStream();
//    void stopStream();
    void startRTC(@NonNull String tempChannel);
    void stopRTC();
    void onResume();
    void onPause();
    boolean isPlayering();
    boolean isRCT();
    interface OnStateListener{
        void onStreamStart();
        void onSuccess();
        void onFailed(String msg);
        void onRTCSuccess();
        void onRTCFailed(String msg);
    }
}
