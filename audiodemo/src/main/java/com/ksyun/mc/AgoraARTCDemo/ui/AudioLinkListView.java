package com.ksyun.mc.AgoraARTCDemo.ui;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xiaoqiang on 2017/8/13.
 */

public class AudioLinkListView extends RelativeLayout {

    private int oViewLeftMargin = 20;
    private int tViewLineHeight = oViewLeftMargin;
    private int moViewMargin = 20;
    private int moViewLHeight = moViewMargin + 15;
    private int moViewLWidth = moViewMargin + 15;
    private Map<Object,View> views = new HashMap<Object,View>();

    public AudioLinkListView(@NonNull Context context) {
        this(context, null);
    }

    public AudioLinkListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioLinkListView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

    }

    public void addView(View view) {

    }

    public void addImageUrl(String url, int defaultResourceID) {
        ImageView imageView = new ImageView(getContext());
        imageView.setBackgroundColor(0x00000000);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        views.put(url,imageView);
        this.addView(imageView, params);
        Glide.with(getContext()).load(url).placeholder(defaultResourceID).error(defaultResourceID).dontAnimate().into(imageView);
    }

    /**
     * 更新界面
     * @param oldUrl
     * @param newUrl
     * @param defaultResourceID
     */
    public void updateImageUrl(String oldUrl,String newUrl, int defaultResourceID){
        View view =  views.get(oldUrl);
        if(view != null && view instanceof ImageView){
            Glide.with(getContext()).load(newUrl).placeholder(defaultResourceID).error(defaultResourceID).dontAnimate().into((ImageView) view);
        }
    }

    public void addImageResource(int resourceID) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(resourceID);
        imageView.setBackgroundColor(0x00000000);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.addView(imageView, params);
        imageView.setTag(resourceID);
    }

    public void removeImageResource(int resourceID) {
        View view = findViewWithTag(resourceID);
        if(view != null){
            removeView(view);
        }
    }
    public void removeImageUrl(String url){
        View view = views.get(url);
        if(view != null){
            removeView(view);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int child = this.getChildCount();
        Log.d("wqq", "onLayout:child=" + child);
        if (child <= 0) {
            super.onLayout(changed, left, top, right, bottom);
        } else {
            int moViewLeftMargin = moViewMargin;
            int moViewLineHeight = moViewLHeight;
            int moViewLineWidth = moViewLWidth;
            switch (child) {
                case 1:
                    updateOneView();
                    return;
                case 2:
                    updateTwoView();
                    return;
                case 3:
                case 4:
                    moViewLeftMargin = moViewLeftMargin + 5;
                    moViewLineHeight = moViewLineHeight + 10;
                    moViewLineWidth = moViewLineWidth - 5;
                    break;
                case 5:
                case 6:
                    moViewLeftMargin = moViewLeftMargin + 6;
                    moViewLineHeight = -1;
                    moViewLineWidth = moViewLineWidth - 15;
                    break;
                case 7:
                case 8:
                    moViewLeftMargin = moViewLeftMargin + 20;
                    moViewLineHeight = -1;
                    moViewLineWidth = moViewLineWidth - 15;
                    break;
                default:
                    return;
            }
            updateMoreView(moViewLeftMargin, moViewLineHeight, moViewLineWidth);
        }
    }

    private void updateOneView() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int childW = width - dpToPx(oViewLeftMargin * 2);
        int left = (int) ((width - childW) / 2.0);
        int top = (int) ((height - childW) / 2.0);
        getChildAt(0).layout(left, top, childW + left, childW + top);
    }

    private void updateTwoView() {
        int width = getMeasuredWidth();
        int childW = (getMeasuredHeight() - dpToPx(tViewLineHeight)) / 2;
        int left = (int) ((width - childW) / 2.0);
        getChildAt(0).layout(left, 0, childW + left, childW);
        getChildAt(1).layout(left, childW + dpToPx(tViewLineHeight), childW + left, 2 * childW + dpToPx(tViewLineHeight));
    }

    private void updateMoreView(int moViewLeftMargin, int moViewLineHeight, int moViewLineWidth) {
        moViewLeftMargin = dpToPx(moViewLeftMargin);
        if (moViewLineHeight > 0) moViewLineHeight = dpToPx(moViewLineHeight);
        moViewLineWidth = dpToPx(moViewLineWidth);

        int width = getMeasuredWidth();
        int childW = (int) ((width - moViewLineWidth - 2 * moViewLeftMargin) / 2.0);
        if (moViewLineHeight < 0) {
            int lines = getChildCount() / 2 + getChildCount() % 2;
            moViewLineHeight = (getMeasuredHeight() - lines * childW) / (lines - 1);
            if (moViewLineHeight < 10) {
                moViewLineHeight = 10;
                childW = (getMeasuredHeight() - (lines - 1) * moViewLineHeight) / lines;
                moViewLeftMargin = (width - 2 * childW - moViewLineWidth) / 2;
            }
        }

        for (int i = 0; i < getChildCount(); i++) {
            int l = moViewLeftMargin + (i % 2) * (childW + moViewLineWidth);
            int t = (i / 2) * (childW + moViewLineHeight);
            int r = childW + l;
            int b = childW + t;
            if (getChildCount() % 2 == 1 && i == (getChildCount() - 1)) {
                l = (int) ((width - childW) / 2.0);
                r = childW + (int) ((width - childW) / 2.0);
            }
            getChildAt(i).layout(l, t, r, b);
        }
    }


    private int dpToPx(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
