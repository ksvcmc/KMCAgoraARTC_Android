package com.ksyun.mc.AgoraARTCDemo.AudioStream;

import com.ksyun.mc.AgoraARTCDemo.utils.Constant;
import com.ksyun.mc.AgoraARTCDemo.utils.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by xiaoqiang on 2017/8/14.
 */

public class AudioStreamUilts {

    public static void joinRoom(String roomName, String uid, HttpRequest.HttpResponseListener listener) {
        request("/api/live/join",roomName,uid,listener);
    }
    public static void leaveRoom(String roomName,String uid,HttpRequest.HttpResponseListener listener){
        request("/api/live/leave",roomName,uid,listener);
    }
    public static void joinChat(String roomName,String uid,HttpRequest.HttpResponseListener listener){
        request("/api/live/chat",roomName,uid,listener);
    }
    public static void unJoinChat(String roomName,String uid,HttpRequest.HttpResponseListener listener){
        request("/api/live/unchat",roomName,uid,listener);
    }
    public static void anchorKick(String roomName,String uid,String anchorId,HttpRequest.HttpResponseListener listener){
        if (listener != null && roomName != null && uid != null&&anchorId != null) {
            HttpRequest httpRequest = new HttpRequest(listener);
            httpRequest.setRequestMethod("POST");
            StringBuilder uri = new StringBuilder(Constant.SERVER_URL);
            uri.append("/api/live/removeLinkChat");
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("roomName", roomName);
                parameters.put("userId", uid);
                parameters.put("anchorId",anchorId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpRequest.execute(uri.toString(), parameters.toString());
        }
    }
    public static void getChatList(String roomName,HttpRequest.HttpResponseListener listener){
        if (listener != null && roomName != null ) {
            HttpRequest httpRequest = new HttpRequest(listener);
            httpRequest.setRequestMethod("GET");
            StringBuilder uri = new StringBuilder(Constant.SERVER_URL);
            uri.append("/api/live/getChatList?roomName=");
            try {
                uri.append(URLEncoder.encode(roomName,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpRequest.execute(uri.toString());
        }
    }

    private static void request(String path,String roomName,String uid,HttpRequest.HttpResponseListener listener){
        if (listener != null && roomName != null && uid != null) {
            HttpRequest httpRequest = new HttpRequest(listener);
            httpRequest.setRequestMethod("POST");
            StringBuilder uri = new StringBuilder(Constant.SERVER_URL);
            uri.append(path);
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
