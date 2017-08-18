package com.ksyun.mc.AgoraARTCDemo.AudioStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ksyun.mc.AgoraARTCDemo.AudioChat.AudioChatActivity;
import com.ksyun.mc.AgoraARTCDemo.R;
import com.ksyun.mc.AgoraARTCDemo.info.ChatInfo;
import com.ksyun.mc.AgoraARTCDemo.info.MeLiveInfo;
import com.ksyun.mc.AgoraARTCDemo.kit.KMCStreamer;
import com.ksyun.mc.AgoraARTCDemo.kit.KMCStreamerManager;
import com.ksyun.mc.AgoraARTCDemo.ui.GlideCircleTransform;
import com.ksyun.mc.AgoraARTCDemo.ui.LoadingDialog;
import com.ksyun.mc.AgoraARTCDemo.ui.NoDoubleClickListener;
import com.ksyun.mc.AgoraARTCDemo.utils.Constant;
import com.ksyun.mc.AgoraARTCDemo.utils.DefaultHttpResponseListener;
import com.ksyun.mc.AgoraARTCDemo.utils.HttpRequest;
import com.ksyun.mc.AgoraARTCDemo.utils.Utils;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by xiaoqiang on 2017/8/14.
 */

public class AudioStreamActivity extends Activity {
    private final static String TAG = AudioStreamActivity.class.getName();
    private final static String INTENT_EXTRA = MeLiveInfo.class.getName();
    private final static String ROOM_NAME = "roomName";
    private final static String USER_ID = "userID";

    private MeLiveInfo info;
    private ImageView userImage;
    private ImageView callImage;
    private TextView userName;
    private SimpleDateFormat timeFormat;
    private TextView timeView;
    private LinearLayout fansLayout;
    private String roomName;
    private String userID;
    private boolean isChat;
    private Handler mHandler;
    private long mTimeMs;

    private KMCStreamer mStreamer;
    private boolean isRemote = false;


    public static void startActivity(Context context, String roomName, String userID, MeLiveInfo info) {
        Intent intent = new Intent(context, AudioStreamActivity.class);

        intent.putExtra(INTENT_EXTRA, info);
        intent.putExtra(ROOM_NAME, roomName);
        intent.putExtra(USER_ID, userID);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.audio_stream_activity);
        info = getIntent().getParcelableExtra(INTENT_EXTRA);
        roomName = getIntent().getStringExtra(ROOM_NAME);
        userID = getIntent().getStringExtra(USER_ID);
        isChat = (info.getUserType() == 1);
        mTimeMs = info.getCreateTime();
        timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        if (info == null) {
            makeToast("参数传递错误");
            info = new MeLiveInfo();
        }
        userImage = (ImageView) findViewById(R.id.imgv_userName);
        timeView = (TextView) findViewById(R.id.tv_time);
        userName = (TextView) findViewById(R.id.tv_userName);
        fansLayout = (LinearLayout) findViewById(R.id.ll_fans);
        callImage = (ImageView) findViewById(R.id.imgv_call);
        mHandler = new Handler();

        findViewById(R.id.imgv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        callImage.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (isChat) {
                    isChat = false;
                    unJoinChat();
                    callImage.setImageResource(R.mipmap.audio_connect);
                } else {
                    joinChat();
                }
            }
        });

        mStreamer = KMCStreamerManager.createKMCStream(info.getUserType() == 1);
        mStreamer.initStream(roomName, this, onStateListener);
        LoadingDialog.showLoadingDialog(this);
        userName.setText(info.getAnchorNickname());
        setTime(info.getCreateTime());
        if (isChat) {
            callImage.setImageResource(R.mipmap.audio_disconnect);
        } else {
            callImage.setImageResource(R.mipmap.audio_connect);
        }
        if (info.getUserType() == 1) {
            callImage.setVisibility(View.INVISIBLE);
        }
        Glide.with(this).load(info.getAnchorHeadUrl()).dontAnimate().transform(new GlideCircleTransform(this)).error(R.mipmap.user_image).placeholder(R.mipmap.user_image).into(userImage);
        updateListUser(info.getFansInfos());
        mHandler.postDelayed(new RunGetChatList(this), Constant.DELAYED_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStreamer.destroyStream();
        leaveRoom();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mStreamer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStreamer.onPause();
    }

    /**
     * 退出房间
     */
    private void leaveRoom() {
        AudioStreamUilts.leaveRoom(roomName, userID, new HttpRequest.HttpResponseListener() {
            @Override
            public void onHttpResponse(int responseCode, String response) {
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Leave Room faile:" + responseCode + ",response:" + response);
                } else {
                    Log.w(TAG, "成功退出房间，response：" + response);
                }
            }
        });
    }

    /**
     * 获取连麦列表
     */
    private void getChatList() {

        AudioStreamUilts.getChatList(roomName, new DefaultHttpResponseListener() {
            @Override
            public void onSuccess(MeLiveInfo info) {
                if(AudioStreamActivity.this.isFinishing()) return;
                if (info.getIsClose() != 0) {
                    makeToast("主播离开房间了, 自动断开直播");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                } else {
                    updateListUser(info.getFansInfos());
                    if (mHandler != null) {
                        mHandler.postDelayed(new RunGetChatList(AudioStreamActivity.this), Constant.DELAYED_TIME);
                    }
                }
            }

            @Override
            public void onFaile(int errorCode, String message) {
                Log.e(TAG, "getChatList  faile:" + errorCode + ",response:" + message);
                if(AudioStreamActivity.this.isFinishing()) return;
                if (mHandler != null) {
                    mHandler.postDelayed(new RunGetChatList(AudioStreamActivity.this), Constant.DELAYED_TIME);
                }
            }
        });
    }

    /**
     * 主播踢出用户
     */
    private void kickUser(String uid) {
        LoadingDialog.showLoadingDialog(AudioStreamActivity.this);
        AudioStreamUilts.anchorKick(roomName, uid, userID, new DefaultHttpResponseListener() {
            @Override
            public void onSuccess(MeLiveInfo info) {
                if(AudioStreamActivity.this.isFinishing()) return;
                LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
                if (info.getIsClose() != 0) {
                    Log.e(TAG, "踢出用户失败，房间已经被关闭");
                }
                updateListUser(info.getFansInfos());
            }

            @Override
            public void onFaile(int errorCode, String error) {
                if(AudioStreamActivity.this.isFinishing()) return;
                LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
                makeToast("踢出用户失败," + error);
            }
        });
    }

    /**
     * 退出连麦
     */
    private void unJoinChat() {
        LoadingDialog.showLoadingDialog(AudioStreamActivity.this);
        Log.d(TAG,"unJoinChat,StopRTC");
        mStreamer.stopRTC();
        AudioStreamUilts.unJoinChat(roomName, userID, new DefaultHttpResponseListener() {
            @Override
            public void onSuccess(MeLiveInfo info) {
                if(AudioStreamActivity.this.isFinishing()) return;
                LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
                if (info.getIsClose() != 0) {
                    Log.e(TAG, "退出连麦，房间已经被关闭");
                }
                updateListUser(info.getFansInfos());
            }

            @Override
            public void onFaile(int errorCode, String error) {
                if(AudioStreamActivity.this.isFinishing()) return;
                LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
//                makeToast("退出连麦失败," + error);
            }
        });
    }

    /**
     * 加入房间
     */
    private void joinChat() {
        LoadingDialog.showLoadingDialog(AudioStreamActivity.this);
        mStreamer.startRTC(roomName);
    }

    private void updateListUser(List<ChatInfo> chatInfos) {
        runOnUiThread(new UpdateUIRunnable(this, chatInfos));
    }

    /**
     * 更新连麦列表
     *
     * @param chatInfos
     */
    private void updateUI(List<ChatInfo> chatInfos) {
        if (chatInfos != null) {
            fansLayout.removeAllViews();
            int count = (chatInfos.size() > 3) ? 3 : chatInfos.size();
            boolean isFansConnect = false;
            for (int i = 0; i < count; i++) {
                ChatInfo in = chatInfos.get(i);
                View view = View.inflate(this, R.layout.audio_stream_user_list_item, null);
                view.setTag(in.getUserId());

                if (userID.equals(in.getUserId())) {
                    isFansConnect = true;
                }
                ImageView status = (ImageView) view.findViewById(R.id.imgv_fans_status);
                ImageView fans = (ImageView) view.findViewById(R.id.imgv_fans);

                Glide.with(this).load(in.getHeadUrl()).placeholder(R.mipmap.user_image).error(R.mipmap.user_image).dontAnimate().transform(new GlideCircleTransform(this)).into(fans);
                if (info.getUserType() == 0 && Utils.getDeviceID(getApplicationContext()).equals(in.getUserId())) {
                    status.setImageResource(R.mipmap.audio_voip_disconnect);
                    view.setOnClickListener(listener);
                } else if (info.getUserType() == 1) { //主播模式
                    status.setImageResource(R.mipmap.audio_voip_disconnect);
                    view.setOnClickListener(listener);
                } else {
                    status.setImageResource(R.mipmap.audio_voip_connect);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                params.weight = 1;
                fansLayout.addView(view, params);
            }
            if (!isFansConnect && (info.getUserType() == 0)) {
                if (mStreamer != null && !mStreamer.isPlayering() && mStreamer.isRCT() && isRemote) {
                    Log.d(TAG,"updateUI,StopRTC");
                    mStreamer.stopRTC();
                    isChat = false;
                    callImage.setImageResource(R.mipmap.audio_connect);
                }
            }
        }
    }

    private NoDoubleClickListener listener = new NoDoubleClickListener() {
        @Override
        public void onNoDoubleClick(View v) {
            if (info.getUserType() == 1) { //主播
                kickUser((String) v.getTag());
            } else if (userID.equals(v.getTag())) { //当前粉丝
                isChat = false;
                unJoinChat();
                callImage.setImageResource(R.mipmap.audio_connect);
            }
        }
    };
    private KMCStreamer.OnStateListener onStateListener = new KMCStreamer.OnStateListener() {
        @Override
        public void onSuccess() {
            Log.i(TAG, "ONSuccess");
            LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
            if (mHandler != null) {
                mHandler.postDelayed(new RunTime(AudioStreamActivity.this), 1000);
            }
        }

        @Override
        public void onFailed(String msg) {
            Log.i(TAG, "onFailed,msg:" + msg);
            LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
            makeToast(msg);
        }

        @Override
        public void onRTCSuccess() {
            Log.i(TAG, "onRTCSuccess");
            isRemote = false;
            AudioStreamUilts.joinChat(roomName, userID, new DefaultHttpResponseListener() {
                @Override
                public void onSuccess(MeLiveInfo info) {
                    LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
                    if (info.getIsClose() == 0) {
                        if(mHandler != null){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    isChat = true;
                                    callImage.setImageResource(R.mipmap.audio_disconnect);
                                }
                            });
                        }
                        isRemote = true;
                        updateListUser(info.getFansInfos());
                    } else {
                        makeToast("加入连麦失败，房间已经被关闭");
                        if(mStreamer != null){
                            Log.d(TAG,"onRTCSuccess,onSuccess,StopRTC");
                            mStreamer.stopRTC();
                        }
                        isRemote = false;
                    }
                }

                @Override
                public void onFaile(int errorCode, String error) {
                    LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
                    makeToast("加入连麦失败," + error);
                    isRemote = false;
                    if(mStreamer != null){
                        Log.d(TAG,"onRTCSuccess,onFaile,StopRTC");
                        mStreamer.stopRTC();
                    }
                }
            });
        }

        @Override
        public void onRTCFailed(String msg) {
            Log.i(TAG, "onRTCFailed,msg:" + msg);
            LoadingDialog.dismissLoadingDialog(AudioStreamActivity.this);
            makeToast(msg);
        }
    };

    private void makeToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AudioStreamActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTime(long ms) {
        String data = getResources().getString(R.string.audio_stream_time);
        data = String.format(data, timeFormat.format(ms));
        timeView.setText(data);
    }

    static class UpdateUIRunnable implements Runnable {
        private WeakReference<AudioStreamActivity> reference;
        private WeakReference<List<ChatInfo>> chatReference;

        UpdateUIRunnable(AudioStreamActivity context, List<ChatInfo> chatInfos) {
            reference = new WeakReference<AudioStreamActivity>(context);
            chatReference = new WeakReference<List<ChatInfo>>(chatInfos);
        }

        @Override
        public void run() {
            if (reference.get() != null && chatReference.get() != null) {
                reference.get().updateUI(chatReference.get());
            }
        }
    }

    static class RunGetChatList implements Runnable {
        private WeakReference<AudioStreamActivity> reference;

        public RunGetChatList(AudioStreamActivity activity) {
            reference = new WeakReference<AudioStreamActivity>(activity);
        }

        @Override
        public void run() {
            if (reference.get() != null) {
                reference.get().getChatList();
            }
        }
    }

    static class RunTime implements Runnable {
        private WeakReference<AudioStreamActivity> reference;

        public RunTime(AudioStreamActivity activity) {
            reference = new WeakReference<AudioStreamActivity>(activity);
        }

        @Override
        public void run() {
            if (reference.get() != null) {
                reference.get().mTimeMs += 1000;
                reference.get().setTime(reference.get().mTimeMs);
                if (reference.get().mHandler != null)
                    reference.get().mHandler.postDelayed(this, 1000);
            }
        }
    }
}
