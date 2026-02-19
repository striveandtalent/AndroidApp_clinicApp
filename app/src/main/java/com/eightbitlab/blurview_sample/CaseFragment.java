package com.eightbitlab.blurview_sample;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import java.util.Arrays;
import java.util.Collections;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.BlurTarget;

public class CaseFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable task;

    private ConstraintLayout root;
    private BlurView searchBlurView;
    private android.widget.EditText etSearch;
    private RecyclerView rvResult;
    private SimpleStringAdapter adapter;

    public CaseFragment() {
        super(R.layout.fragment_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        root = view.findViewById(R.id.listRoot);
        searchBlurView = view.findViewById(R.id.searchBlurView);
        etSearch = view.findViewById(R.id.etSearch);
        rvResult = view.findViewById(R.id.rvResult);

        // 1) RecyclerView
        adapter = new SimpleStringAdapter();
        rvResult.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResult.setAdapter(adapter);

        // 2) BlurView（复用 Activity 的 target）
        BlurTarget target = requireActivity().findViewById(R.id.target);
        Drawable windowBackground = requireActivity().getWindow().getDecorView().getBackground();

        if (target != null) {
            searchBlurView.setupWith(target)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(18f);
        } else {
            // 兜底：至少不崩（先不模糊）
            // 你也可以 Log 一下看看是不是没找到 target
        }

        // 3) 圆角裁剪（有些机型需要补这两句更稳）
        searchBlurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        searchBlurView.setClipToOutline(true);

        // 4) 对焦动画：中间 -> 顶部
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                moveSearchToTop();
                rvResult.setVisibility(View.VISIBLE);
            } else {
                // 你想失焦回中间就打开这一句
                // moveSearchToCenter();
            }
        });

        // 5) 输入防抖搜索
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null) handler.removeCallbacks(task);

                final String q = s.toString().trim();
                task = () -> {
                    if (q.isEmpty()) {
                        adapter.submitList(Collections.emptyList());
                        return;
                    }

                    // TODO：这里换成真实 API 请求
                    // 先用假数据验证 UI 流程
                    adapter.submitList(Arrays.asList(
                            "搜索词：" + q,
                            q + " - 结果1",
                            q + " - 结果2",
                            q + " - 结果3"
                    ));
                };
                handler.postDelayed(task, 300);
            }
        });

        // 启动就显示搜索框（默认就在中间，不需要额外处理）
        // 如需启动直接弹出键盘：
        // etSearch.requestFocus(); showKeyboard(etSearch);
    }

    private void moveSearchToTop() {
        TransitionManager.beginDelayedTransition(root);

        ConstraintSet set = new ConstraintSet();
        set.clone(root);

        // 贴顶部：给个 margin（可调）
        set.clear(R.id.searchBlurView, ConstraintSet.BOTTOM);
        set.connect(R.id.searchBlurView, ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP, dp(24));

        set.applyTo(root);
    }

    private void moveSearchToCenter() {
        TransitionManager.beginDelayedTransition(root);

        ConstraintSet set = new ConstraintSet();
        set.clone(root);

        // 上下都约束回去，回到中间
        set.connect(R.id.searchBlurView, ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        set.connect(R.id.searchBlurView, ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);

        set.applyTo(root);

        rvResult.setVisibility(View.GONE);
        adapter.submitList(Collections.emptyList());
    }

    private int dp(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
