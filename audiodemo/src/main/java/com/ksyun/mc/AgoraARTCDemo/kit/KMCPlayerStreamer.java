package com.ksyun.mc.AgoraARTCDemo.kit;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ksyun.mc.AgoraARTCDemo.utils.Constant;
import com.ksyun.mc.agoravrtc.KMCAgoraEventListener;
import com.ksyun.mc.agoravrtc.KMCAgoraVRTC;
import com.ksyun.mc.agoravrtc.KMCAuthResultListener;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.IOException;

/**
 * Created by xiaoqiang on 2017/8/16.
 */

public class KMCPlayerStreamer implements KMCStreamer {
    private KSYMediaPlayer mMediaPlayer;
    private KMCAgoraVRTC mRTCWrapper;
    private String mRoomName;
    private OnStateListener mStateListener;
    private boolean mIsRemoteConnected;
    private Handler mHandler;
    private Context mContext;

    KMCPlayerStreamer(){}
    @Override
    public void initStream(@NonNull String roomName,@NonNull Context context,@NonNull OnStateListener listener) {
        this.mRoomName = roomName;
        this.mStateListener = listener;
        mHandler = new Handler(Looper.myLooper());
        this.mContext = context.getApplicationContext();
//        mMediaPlayer = new KSYMediaPlayer.Builder(context.getApplicationContext()).build();
//        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
//        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
//        mMediaPlayer.setOnInfoListener(mOnInfoListener);
//        mMediaPlayer.setOnErrorListener(mOnErrorListener);
//        mMediaPlayer.shouldAutoPlay(false);
        mStateListener.onStreamStart();
        startPlayer();
    }

    @Override
    public void destroyStream() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        if (mRTCWrapper != null) {
            mRTCWrapper.leaveChannel();
            mRTCWrapper.release();
        }
        mRTCWrapper = null;
        stopPlayer();
//        if(mMediaPlayer.isPlaying()){
//            stopPlayer();
//        }
//        if(mMediaPlayer != null)
//            mMediaPlayer.release();
//        mMediaPlayer = null;
    }

    @Override
    public void startRTC(final String tempChannel) {
//        if(mMediaPlayer.isPlaying()){
            stopPlayer();
//        }
        mRTCWrapper = new KMCAgoraVRTC(mContext);
        mRTCWrapper.authorize(Constant.TOKEN, false, new KMCAuthResultListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG,"鉴权成功："+mRTCWrapper);
                if (mRTCWrapper != null) {
                    mRTCWrapper.joinChannel(tempChannel, 0);
                    mRTCWrapper.enableObserver(false);
                }
            }

            @Override
            public void onFailure(int errCode) {
                Log.d(TAG,"鉴权失败，错误码：" + errCode);
                mStateListener.onRTCFailed("鉴权失败，错误码：" + errCode);
            }
        });
        mRTCWrapper.registerEventListener(listener);

    }


    @Override
    public void stopRTC() {
        mIsRemoteConnected = false;
        if (mRTCWrapper != null) {
            mRTCWrapper.leaveChannel();
            mRTCWrapper.release();
            mRTCWrapper = null;
        }
        Log.d(TAG,"停止连麦");
//        startPlayer();
        mStateListener.onStreamStart();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startPlayer();
            }
        },1000);
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {

    }

    @Override
    public boolean isPlayering() {
        return (mMediaPlayer == null)?false: mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isRCT() {
        return mIsRemoteConnected;
    }

    private void stopPlayer(){
        if(mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                Log.d(TAG,"停止播放");
                mMediaPlayer.stop();
            }
            Log.d(TAG,"销毁播放器");
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
    }
    private  void startPlayer(){
        mMediaPlayer = new KSYMediaPlayer.Builder(mContext).build();
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.shouldAutoPlay(false);
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            try {
                Log.d(TAG,"准备开始播放");
                mMediaPlayer.setDataSource(Constant.PULL_STREAM_URL + mRoomName);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            // 播放完成，用户可选择释放播放器
            if(mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }
    };
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//                // 设置视频伸缩模式，此模式为裁剪模式
//                mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                // 开始播放视频
                Log.d(TAG,"开始播放");
                mMediaPlayer.start();
            }
        }
    };
    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            if(IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START == i){
                Log.d(TAG,"播放成功");
                mStateListener.onSuccess();
            }
            return false;
        }
    };
    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.e(TAG,"播放出错了,error:"+i);
            mStateListener.onFailed("播放出错了,error:"+i);
            return false;
        }
    };
    private KMCAgoraEventListener listener = new KMCAgoraEventListener() {
        @Override
        public void onEvent(int event, Object... data) {
            switch (event) {
                case JOIN_CHANNEL_RESULT:
                    mStateListener.onRTCSuccess();
                    mIsRemoteConnected = true;
                    Log.d(TAG, "EVENT：JOIN_CHANNEL_RESULT");
                    AudioManager audioManager = (AudioManager) KMCPlayerStreamer.this.mContext.getSystemService(Context.AUDIO_SERVICE);
                    if (!audioManager.isSpeakerphoneOn()) {
                        audioManager.setSpeakerphoneOn(true);//打开扬声器
                    }
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    break;
                case USER_JOINED:
                    Log.d(TAG, "EVENT：USER_JOINED");
                    break;
                case ERROR:
                    Log.d(TAG,"连麦失败，错误码：" + data[0]);
                    mStateListener.onRTCFailed("连麦失败，错误码：" + data[0]);
                    break;
                case LEAVE_CHANNEL:
                    break;
                case USER_OFFLINE:
                    Log.d(TAG, "EVENT：USER_OFFLINE");
                    break;
            }
        }
    };
}
