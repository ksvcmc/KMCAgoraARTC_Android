package com.ksyun.mc.AgoraARTCDemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.mc.AgoraARTCDemo.AudioChat.AudioChatActivity;
import com.ksyun.mc.AgoraARTCDemo.AudioChat.AudioChatUilts;
import com.ksyun.mc.AgoraARTCDemo.AudioStream.AudioStreamActivity;
import com.ksyun.mc.AgoraARTCDemo.AudioStream.AudioStreamUilts;
import com.ksyun.mc.AgoraARTCDemo.info.ChatInfo;
import com.ksyun.mc.AgoraARTCDemo.info.MeLiveInfo;
import com.ksyun.mc.AgoraARTCDemo.ui.ExplainWindow;
import com.ksyun.mc.AgoraARTCDemo.ui.LoadingDialog;
import com.ksyun.mc.AgoraARTCDemo.ui.NoDoubleClickListener;
import com.ksyun.mc.AgoraARTCDemo.utils.DefaultHttpResponseListener;
import com.ksyun.mc.AgoraARTCDemo.utils.Utils;

import java.util.ArrayList;

/**
 * Created by xiaoqiang on 2017/8/11.
 */

public class MainActivity extends Activity {
    private final static int PERMISSION_REQUEST_AUDIOREC = 1;
    private EditText mRoomNameEditText;
    private Button mStartChatButton;
    private Button mStartStreamButton;
    private String mRoomName;
    private TextView mBtnExplain;
    private ExplainWindow mExplainWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_activity);

        mRoomNameEditText = (EditText) findViewById(R.id.audio_chat_channel_name);
        mRoomNameEditText.requestFocus();
        mStartChatButton = (Button) findViewById(R.id.audio_chat_start_button);
        mStartStreamButton = (Button) findViewById(R.id.audio_stream_start_button);
        mBtnExplain = (TextView) findViewById(R.id.btn_explain);
        mExplainWindow = new ExplainWindow(this);

        mStartChatButton.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                joinChat();
            }
        });

        mStartStreamButton.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                joinStream();
            }
        });
        mBtnExplain.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (!mExplainWindow.isShow())
                    mExplainWindow.show(v);
            }
        });
        startAudioRecordWithPermCheck();
    }

    @Override
    public void onBackPressed() {
        if (mExplainWindow.isShow()) {
            mExplainWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    private void joinStream() {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        mRoomName = mRoomNameEditText.getText().toString();
        if (mRoomName != null && mRoomName.length() > 0 && mRoomName.length() < 32) {
            LoadingDialog.showLoadingDialog(this);
            AudioStreamUilts.joinRoom(mRoomName, Utils.getDeviceID(getApplicationContext()), new DefaultHttpResponseListener() {
                @Override
                public void onSuccess(MeLiveInfo info) {
                    if (!MainActivity.this.isFinishing()) {
                        AudioStreamActivity.startActivity(MainActivity.this, mRoomName, Utils.getDeviceID(MainActivity.this), info);
                        LoadingDialog.dismissLoadingDialog(MainActivity.this);
                    }
                }

                @Override
                public void onFaile(int errorCode, String error) {
                    LoadingDialog.dismissLoadingDialog(MainActivity.this);
                    makeToast("创建直播流失败：" + error);
                }
            });
        } else {
            makeToast("标题名称必须在0～32个字符以内");
        }
    }

    private void joinChat() {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        mRoomName = mRoomNameEditText.getText().toString();
        if (mRoomName != null && mRoomName.length() > 0 && mRoomName.length() < 32) {
            LoadingDialog.showLoadingDialog(MainActivity.this);
            AudioChatUilts.joinRoom(mRoomName, Utils.getDeviceID(getApplicationContext()), new DefaultHttpResponseListener() {
                @Override
                public void onSuccess(MeLiveInfo info) {
                    if (!MainActivity.this.isFinishing()) {
                        AudioChatActivity.startActivity(MainActivity.this, mRoomName, Utils.getDeviceID(MainActivity.this), (ArrayList<ChatInfo>) info.getFansInfos());
                        LoadingDialog.dismissLoadingDialog(MainActivity.this);
                    }
                }

                @Override
                public void onFaile(int errorCode, String error) {
                    LoadingDialog.dismissLoadingDialog(MainActivity.this);
                    makeToast("创建聊天室失败：" + error);
                }
            });
        } else {
            makeToast("标题名称必须在0～32个字符以内");
        }
    }

    private void makeToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startAudioRecordWithPermCheck() {
        int audioPerm = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);
        if (audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Toast.makeText(this, "No AudioRecord permission, please check", Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_AUDIOREC);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "No AudioRecord permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
