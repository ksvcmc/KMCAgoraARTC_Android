package com.ksyun.mc.AgoraARTCDemo.utils;

import android.util.Log;

import com.ksyun.mc.AgoraARTCDemo.info.ChatInfo;
import com.ksyun.mc.AgoraARTCDemo.info.MeLiveInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析JSON为MeLiveInfo
 */

public abstract class DefaultHttpResponseListener implements HttpRequest.HttpResponseListener {

    private static final String TAG = DefaultHttpResponseListener.class.getName();

    public abstract void onSuccess(MeLiveInfo info);

    public abstract void onFaile(int errorCode, String message);

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
                onSuccess(info);

            } catch (JSONException ex) {
                Log.e(TAG, "onHttpResponse: " + ex.getLocalizedMessage());
                onFaile(-1, "error:" + ex.getMessage());
            }
        } else {
            if (responseCode > HttpURLConnection.HTTP_INTERNAL_ERROR) {
                onFaile(responseCode, "网络错误");
            } else {
                JSONObject error;
                try {
                    error = new JSONObject(response).getJSONObject("Error");
                    if (error != null) {
                        String message = error.getString("Message");
                        onFaile(responseCode, message);
                    }
                } catch (JSONException e) {
                    onFaile(responseCode, "网络错误");
                }
            }
        }
    }

}
