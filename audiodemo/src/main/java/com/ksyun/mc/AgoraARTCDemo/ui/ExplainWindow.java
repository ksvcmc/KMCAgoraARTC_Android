package com.ksyun.mc.AgoraARTCDemo.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksyun.mc.AgoraARTCDemo.R;

/**
 * Created by xiaoqiang on 2017/8/17.
 */

public class ExplainWindow extends RelativeLayout {

    private PopupWindow popupWindow;

    public ExplainWindow(Context context) {
        this(context,null);
    }

    public ExplainWindow(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ExplainWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    private void initView(){
        this.setBackgroundResource(R.drawable.explain_bg);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
        textView.setTextColor(0xffffffff);
        textView.setText(getResources().getString(R.string.explain));
        textView.setLineSpacing(0,1.2f);
        params.leftMargin = dpToPx(15);
        params.rightMargin = dpToPx(15);
        params.topMargin = dpToPx(25);
        params.bottomMargin = dpToPx(5);
        params.addRule(CENTER_HORIZONTAL);
        this.addView(textView,params);

        ImageView close = new ImageView(getContext());
        close.setImageResource(R.mipmap.close_stream);
        close.setBackgroundColor(0x00000000);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        close.setPadding(dpToPx(5),dpToPx(5),dpToPx(5),dpToPx(5));
        this.addView(close,params);
        close.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
    public void show(View view) {
        int width = getDisplayWidth() - dpToPx(40);
        int height = (int) (width * 10f / 7);
        popupWindow = new PopupWindow(this, width, height);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00F8F8F8")));
        popupWindow.setContentView(this);
        popupWindow.update();
        popupWindow.showAtLocation(view.getRootView(), Gravity.CENTER, 0, 0);
    }
    public boolean isShow(){
        return (popupWindow == null)?false:popupWindow.isShowing();
    }

    public void dismiss(){
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
        popupWindow = null;
    }
    int getDisplayWidth() {
        return ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
    }

    private int dpToPx(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
