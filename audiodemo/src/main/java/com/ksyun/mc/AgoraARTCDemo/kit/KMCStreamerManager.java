package com.ksyun.mc.AgoraARTCDemo.kit;

/**
 * Created by xiaoqiang on 2017/8/16.
 */

public class KMCStreamerManager {
    public static KMCStreamer createKMCStream(boolean isAnchor){
        if(isAnchor){
            return new KMCPushStreamer();
        }else{
            return new KMCPlayerStreamer();
        }
    }
}
