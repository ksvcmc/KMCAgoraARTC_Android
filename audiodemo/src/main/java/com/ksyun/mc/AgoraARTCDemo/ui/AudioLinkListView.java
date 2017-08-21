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

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义布局：
 * 实现聊天室动态添加头像的功能
 * 最多允许8人进入聊天室
 */

public class AudioLinkListView extends RelativeLayout {

    private final int CHILD_VIEW_ONE_LARGE_ICON = 1; // 只有一人时显示大图
    private final int CHILD_VIEW_TWO_LARGE_ICON = 2; // 只有两人时竖向显示大图
    private final int CHILD_VIEW_TWO_LINES_ICONS = 3; // 三人和四人聊天时显示双排小头像
    private final int CHILD_VIEW_THREE_LINES_ICONS = 5; // 五人和六人聊天时显示三排小头像
    private final int CHILD_VIEW_FOUR_LINES_ICONS = 7; //七人和8人聊天时显示四排小头像
    private int mOneViewLeftMargin = 20; //1人时左边Margin
    private int mTwoViewLineHeight = mOneViewLeftMargin; // 2人时定义行间距
    private int mMoreViewLeftMargin = 20; //2人以上时左边Margin 默认值
    private int mMoreViewLHeight = mMoreViewLeftMargin + 15;//2人以上时行间距默认值
    private int mMoreViewLWidth = mMoreViewLeftMargin + 15; //2人以上时列间距默认值
    private Map<Object, View> mViews = new HashMap<Object, View>();

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

    /**
     * 动态添加图片
     * @param url 加载需要的图片地址
     * @param defaultResourceID  默认显示的资源ID
     */
    public void addImageUrl(String url, int defaultResourceID) {
        ImageView imageView = new ImageView(getContext());
        imageView.setBackgroundColor(0x00000000);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mViews.put(url, imageView);
        this.addView(imageView, params);
        Glide.with(getContext()).load(url).placeholder(defaultResourceID).error(defaultResourceID).dontAnimate().into(imageView);
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
        if (view != null) {
            removeView(view);
        }
    }

    /**
     * 根据URL删除图片
     * @param url
     */
    public void removeImageUrl(String url) {
        View view = mViews.get(url);
        if (view != null) {
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
            int moViewLeftMargin = mMoreViewLeftMargin;
            int moViewLineHeight = mMoreViewLHeight;
            int moViewLineWidth = mMoreViewLWidth;
            switch (child) {
                case CHILD_VIEW_ONE_LARGE_ICON:
                    updateOneView();
                    return;
                case CHILD_VIEW_TWO_LARGE_ICON:
                    updateTwoView();
                    return;
                case CHILD_VIEW_TWO_LINES_ICONS:
                case CHILD_VIEW_TWO_LINES_ICONS + 1:
                    moViewLeftMargin = moViewLeftMargin + 5;
                    moViewLineHeight = moViewLineHeight + 10;
                    moViewLineWidth = moViewLineWidth - 5;
                    break;
                case CHILD_VIEW_THREE_LINES_ICONS:
                case CHILD_VIEW_THREE_LINES_ICONS + 1:
                    moViewLeftMargin = moViewLeftMargin + 6;
                    moViewLineHeight = -1;
                    moViewLineWidth = moViewLineWidth - 15;
                    break;
                case CHILD_VIEW_FOUR_LINES_ICONS:
                case CHILD_VIEW_FOUR_LINES_ICONS + 1:
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

    /**
     * 1人时更新界面
     */
    private void updateOneView() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int childW = width - dpToPx(mOneViewLeftMargin * 2);
        int left = (int) ((width - childW) / 2.0);
        int top = (int) ((height - childW) / 2.0);
        getChildAt(0).layout(left, top, childW + left, childW + top);
    }

    /**
     * 2人时更新界面
     */
    private void updateTwoView() {
        int width = getMeasuredWidth();
        int childW = (getMeasuredHeight() - dpToPx(mTwoViewLineHeight)) / 2;
        int left = (int) ((width - childW) / 2.0);
        getChildAt(0).layout(left, 0, childW + left, childW);
        getChildAt(1).layout(left, childW + dpToPx(mTwoViewLineHeight), childW + left, 2 * childW + dpToPx(mTwoViewLineHeight));
    }

    /**
     * 2人以上时更新界面
     * @param moViewLeftMargin
     * @param moViewLineHeight  行间距为-1时，表示需要把图片铺满
     * @param moViewLineWidth
     */
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
