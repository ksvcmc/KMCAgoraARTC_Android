package com.ksyun.mc.AgoraARTCDemo.kit;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ksyun.mc.AgoraARTCDemo.utils.Constant;
import com.ksyun.mc.agoravrtc.KMCAuthResultListener;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;

import java.lang.ref.WeakReference;

/**
 * Created by xiaoqiang on 2017/8/16.
 */

public class KMCPushStreamer implements KMCStreamer {

    private KMCAgoraStreamer mStreamer;
    private Handler mHandler;
    private OnStateListener mStateListener;
    private String mRoomName;

    KMCPushStreamer() {
    }

    @Override
    public void initStream(@NonNull String roomName, @NonNull Context context,@NonNull final OnStateListener listener) {
        mStreamer = new KMCAgoraStreamer(context);
        this.mStateListener = listener;
        mHandler = new Handler(Looper.getMainLooper());
        this.mRoomName = roomName;
        mStreamer.setAudioOnly(true);
        mStreamer.setUrl(Constant.PUSH_STREAM_URL + roomName);
        mStreamer.setOnInfoListener(infoListener);
        mStreamer.setOnErrorListener(new ErrorListener(this));
        mStreamer.setOnLogEventListener(logListener);
//        mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
        mStreamer.setAudioSampleRate(44100);
        mStreamer.setAudioKBitrate(48);
        mStreamer.setEnableAudioPreview(false);
        mStateListener.onStreamStart();
        mStreamer.authorize(Constant.TOKEN, new KMCAuthResultListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"鉴权成功："+mStreamer);
                if(mStreamer != null) {
                    mStreamer.startStream();
                }
            }

            @Override
            public void onFailure(int errCode) {
                Log.e(TAG, "鉴权失败，onFailure:"+errCode);
                listener.onFailed("鉴权失败: 错误码" + errCode);
            }
        });
    }

    @Override
    public void destroyStream() {
        Log.e(TAG, "destroyStream");
        stopRTC();
        if (mStreamer.isRecording()) {
            mStreamer.stopStream();
        }
        mStreamer.setOnLogEventListener(null);
        mStreamer.release();
        mStreamer = null;
    }

    @Override
    public void startRTC(String tempChannel) {
        if (mStreamer != null) {
            Log.i(TAG, "startRTC,Channel:"+tempChannel);
            mStreamer.startRTC(tempChannel);
            mStateListener.onSuccess();
            mStateListener.onRTCSuccess();
            mStreamer.setAudioMode(AudioManager.MODE_NORMAL);
        }
    }

    @Override
    public void stopRTC() {
        if (mStreamer != null) {
            Log.i(TAG, "stopRTC");
            mStreamer.stopRTC();
//            mStateListener.onRTCFailed("结束连麦");
        }
    }

    @Override
    public void onResume() {
        mStreamer.onResume();
    }

    @Override
    public void onPause() {
        mStreamer.onPause();
    }

    @Override
    public boolean isPlayering() {
        return true;
    }

    @Override
    public boolean isRCT() {
        return mStreamer.isRemoteConnected();
    }


    private KSYStreamer.OnInfoListener infoListener = new KSYStreamer.OnInfoListener() {

        @Override
        public void onInfo(int what, int i1, int i2) {
            Log.i(TAG, "INFO,what:" + what);
            switch (what) {
                case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                    Log.i(TAG, "KSY_STREAMER_OPEN_STREAM_SUCCESS");
                    startRTC(mRoomName);
                    break;

            }
        }
    };

    private static class ErrorListener implements KSYStreamer.OnErrorListener {
        private WeakReference<KMCPushStreamer> reference;

        ErrorListener(KMCPushStreamer pushStream) {
            reference = new WeakReference<KMCPushStreamer>(pushStream);
        }

        @Override
        public void onError(int what, int i1, int i2) {
            Log.i(TAG, "onError,what:" + what);
            switch (what) {
                case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                    if (reference.get() != null) {
                        reference.get().mStateListener.onFailed("推流失败,错误：" + what);
                    }
                    break;
                default:
                    if (reference.get() != null) {
                        reference.get().mStateListener.onFailed("推流失败，错误：" + what+"，正在重试");
                    }
                    if (reference.get() != null && reference.get().mStreamer != null && !reference.get().mStreamer.getEnableAutoRestart()) {
                        reference.get().mStreamer.stopStream();
                        reference.get().mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (reference.get() != null && reference.get().mStreamer != null)
                                    reference.get().mStreamer.startStream();
                            }
                        }, 3000);
                    }
                    break;
            }
        }
    }

    ;
    private static StatsLogReport.OnLogEventListener logListener = new StatsLogReport.OnLogEventListener() {
        @Override
        public void onLogEvent(StringBuilder stringBuilder) {
            Log.i(TAG, "***onLogEvent : " + stringBuilder.toString());
        }
    };
}
