package com.eightbitlab.blurview_sample;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

/**
 * 病例页：
 * 1) 页面内展示可滚动内容；
 * 2) 顶部搜索框使用 BlurView 做“实时高斯模糊”；
 * 3) 使用页面内部的 BlurTarget 作为采样源，避免跨层级采样导致问题。
 */
public class CaseFragment extends Fragment {
    public CaseFragment() {
        super(R.layout.fragment_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BlurView topBlurView = view.findViewById(R.id.topBlurView);

        /**
         * 重点：这里必须使用“当前 Fragment 内部”的 BlurTarget。
         * 不要直接采样 Activity 级 target，否则可能出现：
         * - 自引用/跨层采样
         * - UI 异常甚至崩溃
         */
        BlurTarget target = view.findViewById(R.id.caseBlurTarget);
        if (topBlurView == null || target == null) {
            return;
        }

        // 与 MainActivity 同理，清屏背景做空值兜底。
        Drawable windowBackground = requireActivity().getWindow().getDecorView().getBackground();
        if (windowBackground == null) {
            windowBackground = new ColorDrawable(Color.TRANSPARENT);
        }

        /**
         * 让 BlurView 真正按圆角裁切：
         * - clipToOutline=true 仅表示“允许按 outline 裁切”；
         * - 还需要明确给出 outline（由背景 shape 提供）。
         */
        topBlurView.setClipToOutline(true);
        topBlurView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view1, Outline outline) {
                if (view1.getBackground() != null) {
                    view1.getBackground().getOutline(outline);
                }
                outline.setAlpha(1f);
            }
        });

        /**
         * 搜索框 BlurView 的完整初始化链路：
         * 1) setupWith(target): 从 caseBlurTarget 采样；
         * 2) setFrameClearDrawable(windowBackground): 提供清屏背景；
         * 3) setBlurRadius(25f): 模糊半径。
         */
        topBlurView.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(25f);

        // 根据系统状态栏高度动态调整搜索框 top margin，避免和状态栏重叠。
        ViewCompat.setOnApplyWindowInsetsListener(topBlurView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = insets.top + dpToPx(12);
            v.setLayoutParams(params);
            return windowInsets;
        });
    }

    /** dp -> px 工具方法，避免硬编码像素。 */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
