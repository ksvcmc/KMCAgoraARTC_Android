package com.ksyun.mc.AgoraARTCDemo.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by xiaoqiang on 2017/8/14.
 * 直播时用户信息
 */

public class MeLiveInfo implements Parcelable {
    private String anchorNickname;
    private String anchorHeadUrl;
    private String headUrl;
    private int userType;
    private long createTime;
    private int isClose;
    private String  streamId;
    private List<ChatInfo> fansInfos;

    public MeLiveInfo(){

    }

    protected MeLiveInfo(Parcel in) {
        anchorNickname = in.readString();
        anchorHeadUrl = in.readString();
        headUrl = in.readString();
        userType = in.readInt();
        createTime = in.readLong();
        fansInfos = in.createTypedArrayList(ChatInfo.CREATOR);
        isClose = in.readInt();
        streamId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(anchorNickname);
        dest.writeString(anchorHeadUrl);
        dest.writeString(headUrl);
        dest.writeInt(userType);
        dest.writeLong(createTime);
        dest.writeTypedList(fansInfos);
        dest.writeInt(isClose);
        dest.writeString(streamId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MeLiveInfo> CREATOR = new Creator<MeLiveInfo>() {
        @Override
        public MeLiveInfo createFromParcel(Parcel in) {
            return new MeLiveInfo(in);
        }

        @Override
        public MeLiveInfo[] newArray(int size) {
            return new MeLiveInfo[size];
        }
    };

    public String getAnchorNickname() {
        return anchorNickname;
    }

    public void setAnchorNickname(String anchorNickname) {
        this.anchorNickname = anchorNickname;
    }

    public String getAnchorHeadUrl() {
        return anchorHeadUrl;
    }

    public void setAnchorHeadUrl(String anchorHeadUrl) {
        this.anchorHeadUrl = anchorHeadUrl;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public List<ChatInfo> getFansInfos() {
        return fansInfos;
    }

    public void setFansInfos(List<ChatInfo> fansInfos) {
        this.fansInfos = fansInfos;
    }

    public int getIsClose() {
        return isClose;
    }

    public void setIsClose(int isClose) {
        this.isClose = isClose;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }
}
