package com.ksyun.mc.AgoraARTCDemo.AudioChat;


import com.ksyun.mc.AgoraARTCDemo.utils.Constant;
import com.ksyun.mc.AgoraARTCDemo.utils.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by sujia on 2017/8/10.
 */

public class AudioChatUilts {
    private final static String TAG = AudioChatUilts.class.getSimpleName();

    public static void joinRoom(String roomName, String uid, HttpRequest.HttpResponseListener listener) {
        if (listener != null && roomName != null) {
            HttpRequest httpRequest = new HttpRequest(listener);
            httpRequest.setConnectTimeout(5000);
            httpRequest.setTimeout(5000);
            httpRequest.setRequestMethod("POST");

            StringBuilder uri = new StringBuilder(Constant.SERVER_URL);
            uri.append("/api/multi/join");
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("roomName", roomName);
                parameters.put("userId", uid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpRequest.execute(uri.toString(), parameters.toString());
        }
    }

    public static void leaveRoom(String roomName, String uid, HttpRequest.HttpResponseListener listener) {
        if (listener != null && roomName != null) {
            if (listener != null && roomName != null) {
                HttpRequest httpRequest = new HttpRequest(listener);
                httpRequest.setConnectTimeout(5000);
                httpRequest.setTimeout(5000);
                httpRequest.setRequestMethod("POST");

                StringBuilder uri = new StringBuilder(Constant.SERVER_URL);
                uri.append("/api/multi/leave");
                JSONObject parameters = new JSONObject();
                try {
                    parameters.put("roomName", roomName);
                    parameters.put("userId", uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                httpRequest.execute(uri.toString(), parameters.toString());
            }
        }
    }

    public static void fetchChatList(String roomName, HttpRequest.HttpResponseListener listener) {
        if (listener != null && roomName != null) {
            HttpRequest httpRequest = new HttpRequest(listener);
            httpRequest.setConnectTimeout(5000);
            httpRequest.setTimeout(5000);
            httpRequest.setRequestMethod("GET");

            StringBuilder uri = new StringBuilder(Constant.SERVER_URL);
            uri.append("/api/multi/getChatList");
            uri.append("?roomName=");
            try {
                uri.append(URLEncoder.encode(roomName,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpRequest.execute(uri.toString());
        }
    }
}
