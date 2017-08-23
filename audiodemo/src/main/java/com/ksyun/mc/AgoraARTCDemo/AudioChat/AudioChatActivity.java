package com.ksyun.mc.AgoraARTCDemo.AudioChat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.mc.AgoraARTCDemo.R;
import com.ksyun.mc.AgoraARTCDemo.info.ChatInfo;
import com.ksyun.mc.AgoraARTCDemo.info.MeLiveInfo;
import com.ksyun.mc.AgoraARTCDemo.ui.AudioLinkListView;
import com.ksyun.mc.AgoraARTCDemo.ui.NoDoubleClickListener;
import com.ksyun.mc.AgoraARTCDemo.utils.Constant;
import com.ksyun.mc.AgoraARTCDemo.utils.DefaultHttpResponseListener;
import com.ksyun.mc.AgoraARTCDemo.utils.HttpRequest;
import com.ksyun.mc.agoravrtc.KMCAgoraEventListener;
import com.ksyun.mc.agoravrtc.KMCAgoraVRTC;
import com.ksyun.mc.agoravrtc.KMCAuthResultListener;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sujia on 2017/8/10.
 */

public class AudioChatActivity extends Activity {
    private final static String TAG = AudioChatActivity.class.getSimpleName();
    private final static String CHAT_ID_LIST = "CHAT_ID_LIST";
    private final static String ROOM_NAME = "ROOM_NAME";
    private final static String USER_ID = "USER_ID";
    private final static String CHANNEL_NAME ="CHANNEL_NAME";


    private ArrayList<ChatInfo> mChatIdList;
    private AudioLinkListView mLinkListView;
    private String mRootName;
    private String mUserID;
    private String mChannelName;
    private ImageView mChatCloseImageView;
    private ImageView mCloseImageView;
    private TextView mTitleTextView;
    private Handler mHandler;

    private boolean mIsRTCRun;
    private KMCAgoraVRTC mRTCWrapper;

    public static void startActivity(Context mContext, String roorName, String userID,String channelName, ArrayList<ChatInfo> chatInfos) {
        Intent intent = new Intent(mContext, AudioChatActivity.class);
        intent.putParcelableArrayListExtra(AudioChatActivity.CHAT_ID_LIST, chatInfos);
        intent.putExtra(ROOM_NAME, roorName);
        intent.putExtra(USER_ID, userID);
        intent.putExtra(CHANNEL_NAME,channelName);
        mContext.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.audio_chat_activity);

        Intent intent = getIntent();
        mChatIdList = intent.getParcelableArrayListExtra(CHAT_ID_LIST);
        if (mChatIdList == null) mChatIdList = new ArrayList<ChatInfo>();
        mRootName = intent.getStringExtra(ROOM_NAME);
        mUserID = intent.getStringExtra(USER_ID);
        mChannelName = intent.getStringExtra(CHANNEL_NAME);
        mLinkListView = (AudioLinkListView) findViewById(R.id.audio_user_list);
        mChatCloseImageView = (ImageView) findViewById(R.id.imgv_chat_close);
        mTitleTextView = (TextView) findViewById(R.id.tv_chat_title);
        mCloseImageView = (ImageView) findViewById(R.id.imgv_close);

        mHandler = new Handler();
        String name = String.format(getResources().getString(R.string.audio_chat_title), mRootName);
        mTitleTextView.setText(name);

        if (mChatIdList != null && mChatIdList.size() > 0) {
            for (ChatInfo info : mChatIdList) {
                mLinkListView.addImageUrl(info.getHeadUrl(), R.mipmap.default_image);
            }
        }
        mChatCloseImageView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
//                hangupChat();
                finish();
            }
        });
        mCloseImageView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
//                hangupChat();
                finish();
            }
        });
        mHandler.postDelayed(runFetchChatList, Constant.DELAYED_TIME);
        mRTCWrapper = new KMCAgoraVRTC(this);
        mRTCWrapper.authorize(Constant.TOKEN, false, new KMCAuthResultListener() {
            @Override
            public void onSuccess() {
                if (mRTCWrapper != null && !isFinishing()) {
                    mRTCWrapper.joinChannel(mChannelName, 0);
                    mRTCWrapper.enableObserver(false);
                }
            }

            @Override
            public void onFailure(int errCode) {
                makeToast("鉴权失败: 错误码" + errCode);
            }
        });
        mRTCWrapper.registerEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hangupChat();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    private Runnable runFetchChatList = new RunFetchChatList(this);

    private KMCAgoraEventListener listener = new KMCAgoraEventListener() {
        @Override
        public void onEvent(int event, Object... data) {
            switch (event) {
                case JOIN_CHANNEL_RESULT:
                    mIsRTCRun = true;
                    Log.d(TAG, "EVENT：JOIN_CHANNEL_RESULT");
                    AudioManager audioManager = (AudioManager) AudioChatActivity.this.getSystemService(Context.AUDIO_SERVICE);
                    if (!audioManager.isSpeakerphoneOn()) {
                        audioManager.setSpeakerphoneOn(true);//打开扬声器
                    }
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    break;
                case USER_JOINED:
                    Log.d(TAG, "EVENT：USER_JOINED");
                    fetchChatList();
                    break;
                case ERROR:
                    mIsRTCRun = false;
                    makeToast("连麦错误，错误码：" + data[0]);
                    break;
                case LEAVE_CHANNEL:
                    mIsRTCRun = false;
                    break;
                case USER_OFFLINE:
                    Log.d(TAG, "EVENT：USER_OFFLINE");
                    fetchChatList();
                    break;
            }
        }
    };

    /**
     * 挂断通话
     */
    private void hangupChat() {
        if (mRTCWrapper != null ) {
//            mRTCWrapper.enableObserver(false);
            if(mIsRTCRun) { //用户成功加入频道后才可以离开频道
                mRTCWrapper.leaveChannel();
            }
            mRTCWrapper.release();
            mRTCWrapper = null;
            AudioChatUilts.leaveRoom(mRootName, mUserID, new HttpRequest.HttpResponseListener() {
                @Override
                public void onHttpResponse(int responseCode, String response) {
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "挂断房间失败，错误吗：" + responseCode + ",错误原因:" + response);
                    }
                }
            });
        }
    }

    /**
     * 查询连麦列表
     */
    private void fetchChatList() {
        AudioChatUilts.fetchChatList(mRootName, new DefaultHttpResponseListener() {
            @Override
            public void onSuccess(MeLiveInfo info) {
                if(!AudioChatActivity.this.isFinishing()) {
                    updateLinkListView(info.getFansInfos());
                    if (mHandler != null) {
                        mHandler.postDelayed(runFetchChatList, Constant.DELAYED_TIME);
                    }
                }
            }

            @Override
            public void onFailed(int errorCode, String error) {
                if(!AudioChatActivity.this.isFinishing()) {
                    Log.e(TAG, "查询连麦用户列表失败，错误吗：" + errorCode + ",错误原因:" + error);
                    if (mHandler != null) {
                        mHandler.postDelayed(runFetchChatList, Constant.DELAYED_TIME);
                    }
                }
            }
        });

    }

    /**
     * 选取出更新的数据，去更新 AudioLinkListView
     *
     * @param newInfos
     */
    private void updateLinkListView(final List<ChatInfo> newInfos) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<ChatInfo> tempList = new ArrayList<ChatInfo>(mChatIdList);
                for (ChatInfo info : tempList) {
                    if (!newInfos.contains(info)) {
                        mLinkListView.removeImageUrl(info.getHeadUrl());
                        mChatIdList.remove(info);
                    }
                }
                for (ChatInfo info : newInfos) {
                    if (!mChatIdList.contains(info)) {
                        mLinkListView.addImageUrl(info.getHeadUrl(), R.mipmap.default_image);
                        mChatIdList.add(info);
                    }
                }
            }
        });
    }

    private void makeToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AudioChatActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class RunFetchChatList implements Runnable {
        private WeakReference<AudioChatActivity> reference;

        public RunFetchChatList(AudioChatActivity activity) {
            reference = new WeakReference<AudioChatActivity>(activity);
        }

        @Override
        public void run() {
            if (reference.get() != null) {
                reference.get().fetchChatList();
            }
        }
    }

}
