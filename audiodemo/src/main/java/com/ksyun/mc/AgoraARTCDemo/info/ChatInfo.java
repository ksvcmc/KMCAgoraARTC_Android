package com.ksyun.mc.AgoraARTCDemo.info;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sujia on 2017/8/11.
 */

public class ChatInfo implements Parcelable {
    private String nickName;
    private String userId;
    private String headUrl;

    public ChatInfo(String nickName, String userId, String headUrl) {
        this.nickName = nickName;
        this.userId = userId;
        this.headUrl = headUrl;
    }

    protected ChatInfo(Parcel in) {
        nickName = in.readString();
        userId = in.readString();
        headUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nickName);
        dest.writeString(userId);
        dest.writeString(headUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatInfo> CREATOR = new Creator<ChatInfo>() {
        @Override
        public ChatInfo createFromParcel(Parcel in) {
            return new ChatInfo(in);
        }

        @Override
        public ChatInfo[] newArray(int size) {
            return new ChatInfo[size];
        }
    };

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChatInfo)) return false;
        ChatInfo info = (ChatInfo) obj;
        boolean f1 = ((nickName == info.nickName) || (nickName != null && nickName.equals(info.nickName)));
        boolean f2 = ((userId == info.userId) || (userId != null && userId.equals(info.userId)));
        boolean f3 = ((headUrl == info.headUrl) || (headUrl != null && headUrl.equals(info.headUrl)));
        if (f1 && f2 && f3) return true;
        return false;
    }

}
