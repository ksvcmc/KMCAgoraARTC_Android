package com.ksyun.mc.AgoraARTCDemo.utils;

import android.util.Log;

import com.ksyun.mc.AgoraARTCDemo.info.ChatInfo;
import com.ksyun.mc.AgoraARTCDemo.info.MeLiveInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 解析JSON为MeLiveInfo
 */

public abstract class DefaultHttpResponseListener implements HttpRequest.HttpResponseListener {

    private static final String TAG = DefaultHttpResponseListener.class.getName();
    public static final int ROOM_FULL = 404; //房间已满
    public static final int ROOM_NAME_ERROR = 400; //房间名称错误或者不存在
    public static final int NETWORK_ERROR = -2; //网络请求错误

    public abstract void onSuccess(MeLiveInfo info);

    public abstract void onFailed(int errorCode, String message);

    @Override
    public void onHttpResponse(int responseCode, String response) {
        if (responseCode == HttpURLConnection.HTTP_OK) {
            MeLiveInfo info = new MeLiveInfo();
            List<ChatInfo> chats = new ArrayList<ChatInfo>();
            JSONObject obj;
            try {
                obj = new JSONObject(response);
                JSONArray array = obj.getJSONArray("chatIdList");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    String nickname = item.getString("nickname");
                    String userId = item.getString("userId");
                    String headUrl = item.getString("headUrl");
                    chats.add(new ChatInfo(nickname, userId, headUrl));
                }
                info = new MeLiveInfo();
                info.setFansInfos(chats);
                info.setAnchorHeadUrl(obj.optString("anchorHeadUrl"));
                info.setAnchorNickname(obj.optString("anchorNickname"));
                info.setHeadUrl(obj.optString("headUrl"));
                info.setUserType(obj.optInt("userType", 0));
                info.setCreateTime(obj.optLong("createTime", 0));
                info.setIsClose(obj.optInt("isClose",0));
                info.setStreamId(obj.optString("streamId",String.valueOf(new Date().getTime())));
                onSuccess(info);

            } catch (JSONException ex) {
                Log.e(TAG, "onHttpResponse: " + ex.getLocalizedMessage());
                onFailed(-1, "error:" + ex.getMessage());
            }
        } else {
            if (responseCode > HttpURLConnection.HTTP_INTERNAL_ERROR) {
                onFailed(NETWORK_ERROR, "网络错误");
            } else {
                JSONObject error;
                try {
                    error = new JSONObject(response).getJSONObject("Error");
                    if (error != null) {
                        String message = error.getString("Message");
                        onFailed(responseCode, message);
                    }
                } catch (JSONException e) {
                    onFailed(-1, "error:" + e.getMessage());
                }
            }
        }
    }

}
