package com.ksyun.mc.AgoraARTCDemo.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ksyun.mc.AgoraARTCDemo.R;

import java.util.Calendar;

/**
 * Created by xiaoqiang on 2017/8/17.
 */

public class LoadingDialog extends Dialog {
    private static LoadingDialog mDialog;
    private long lastClickTime;
    private int back = 0;
    private int backNum;

    private LoadingDialog(Context context, int theme,int backNum) {
        super(context, theme);
        initView();
        this.backNum = backNum;
    }
    private void initView(){
        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setBackgroundColor(0x00000000);
        ProgressBar bar = new ProgressBar(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(bar,params);
        setContentView(relativeLayout);

    }
    public static synchronized void showLoadingDialog(Context mContext,int backPressedNum){
        if(mDialog == null){
            mDialog = new LoadingDialog(mContext,R.style.loading_dialog,backPressedNum);
            mDialog.show();
        }
    }

    public static synchronized void showLoadingDialog(Context mContext){
        showLoadingDialog(mContext,2);
    }

    @Override
    public void onBackPressed() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime < 2000 && isShowing()) {
            back++;
        } else {
            Toast.makeText(getContext(),"在按一次退出",Toast.LENGTH_SHORT).show();
            lastClickTime = currentTime;
            back = 1;
        }
        if(back >= backNum){
            super.onBackPressed();
            if(getContext() instanceof Activity){
                ((Activity)getContext()).finish();
            }
            mDialog = null;
        }
    }

    public static synchronized void dismissLoadingDialog(Context mContext){
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }
        mDialog = null;
    }

}
