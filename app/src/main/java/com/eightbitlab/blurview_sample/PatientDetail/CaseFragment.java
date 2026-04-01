package com.eightbitlab.blurview_sample.PatientDetail;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.Patient_Create.CreatePatientActivity;
import com.eightbitlab.blurview_sample.Patient_Search.PageResult;
import com.eightbitlab.blurview_sample.Patient_Search.PatientAdapter;
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaseFragment extends Fragment {

    private PatientAdapter adapter;
    private TextView tvEmpty;

    private android.animation.ObjectAnimator refreshAnimator;
    private RotateDrawable refreshRotateDrawable;

    private long refreshAnimStartTime = 0L;
    private static final long MIN_REFRESH_ANIM_DURATION = 900L; // 最少显示 0.9 秒
    private static final long REFRESH_ROTATE_DURATION = 500L;   // 转一圈 0.5 秒，更明显

    public CaseFragment() {
        super(R.layout.fragment_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) BlurView 初始化
        BlurView topBlurView = view.findViewById(R.id.topBlurView);
        BlurTarget target = view.findViewById(R.id.caseBlurTarget);

        // 新增按钮
        view.findViewById(R.id.fabAddPatient).setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), CreatePatientActivity.class);
            startActivity(it);
        });

        // 刷新按钮
        FloatingActionButton fabRefresh = view.findViewById(R.id.fabRefreshPatient);

        // 用 RotateDrawable 包住原图标，只旋转图标本身
        Drawable origin = fabRefresh.getDrawable();
        if (origin != null) {
            refreshRotateDrawable = new RotateDrawable();
            refreshRotateDrawable.setDrawable(origin);
            refreshRotateDrawable.setFromDegrees(0f);
            refreshRotateDrawable.setToDegrees(360f);
            refreshRotateDrawable.setLevel(0);
            fabRefresh.setImageDrawable(refreshRotateDrawable);
        }

        fabRefresh.setOnClickListener(v -> {
            if (refreshAnimator != null && refreshAnimator.isRunning()) {
                return;
            }

            EditText searchInput = view.findViewById(R.id.searchInput);
            String kw = searchInput.getText() == null ? "" : searchInput.getText().toString().trim();

            hideKeyboardAndClearFocus(searchInput);
            startRefreshAnim(fabRefresh);
            doSearch(kw, fabRefresh, true);
        });

        if (topBlurView == null || target == null) return;

        Drawable windowBackground = requireActivity().getWindow().getDecorView().getBackground();
        if (windowBackground == null) {
            windowBackground = new ColorDrawable(Color.TRANSPARENT);
        }

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

        topBlurView.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(25f);

        ViewCompat.setOnApplyWindowInsetsListener(topBlurView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = insets.top + dpToPx(12);
            v.setLayoutParams(params);
            return windowInsets;
        });

        // 2) RecyclerView 初始化
        RecyclerView rv = view.findViewById(R.id.rvPatients);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new PatientAdapter(item -> {
            if (item == null || item.patientId == null || item.patientId.isEmpty()) return;

            Intent it = new Intent(requireContext(), PatientDetailActivity.class);
            it.putExtra("patientId", item.patientId);
            startActivity(it);
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // 3) 搜索框监听
        EditText searchInput = view.findViewById(R.id.searchInput);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearch = actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);

            if (isSearch) {
                String kw = v.getText() == null ? "" : v.getText().toString().trim();
                hideKeyboardAndClearFocus(searchInput);
                doSearch(kw, null, false); // 搜索时不提示“刷新成功”
                return true;
            }
            return false;
        });

        // 首次进入页面加载
        doSearch("", null, false);
    }

    /**
     * @param keyword 搜索关键字
     * @param refreshFab 刷新按钮（只有手动刷新时传入，其他场景传 null）
     * @param showRefreshToast 是否显示“刷新成功”提示
     */
    private void doSearch(String keyword,
                          @Nullable FloatingActionButton refreshFab,
                          boolean showRefreshToast) {

        Call<ReturnInfo<PageResult<PatientModel>>> call =
                ApiClient.api().searchPatients(keyword);

        call.enqueue(new Callback<ReturnInfo<PageResult<PatientModel>>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<PageResult<PatientModel>>> call,
                                   @NonNull Response<ReturnInfo<PageResult<PatientModel>>> response) {

                stopRefreshAnim(refreshFab);

                if (!response.isSuccessful()) {
                    adapter.submit(Collections.emptyList());
                    showEmpty(true);
                    Toast.makeText(requireContext(), "HTTP错误：" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<PageResult<PatientModel>> body = response.body();
                if (body == null) {
                    adapter.submit(Collections.emptyList());
                    showEmpty(true);
                    Toast.makeText(requireContext(), "响应为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (body.status != 0) {
                    adapter.submit(Collections.emptyList());
                    showEmpty(true);
                    Toast.makeText(requireContext(),
                            body.message == null ? "查询失败" : body.message,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 业务成功
                List<PatientModel> items = (body.data == null) ? null : body.data.items;
                boolean isEmpty = (items == null || items.isEmpty());

                if (isEmpty) {
                    adapter.submit(Collections.emptyList());
                    showEmpty(true);
                } else {
                    adapter.submit(items);
                    showEmpty(false);
                }

                if (showRefreshToast) {
                    Toast.makeText(
                            requireContext(),
                            isEmpty ? "刷新成功，暂无数据" : "刷新成功",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<PageResult<PatientModel>>> call,
                                  @NonNull Throwable t) {

                stopRefreshAnim(refreshFab);
                adapter.submit(Collections.emptyList());
                showEmpty(true);
                Toast.makeText(requireContext(), "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 启动刷新动画
    private void startRefreshAnim(@Nullable FloatingActionButton refreshFab) {
        if (refreshFab == null || refreshRotateDrawable == null) return;

        if (refreshAnimator != null && refreshAnimator.isRunning()) {
            return;
        }

        refreshAnimStartTime = System.currentTimeMillis();

        refreshAnimator = android.animation.ObjectAnimator.ofInt(
                refreshRotateDrawable,
                "level",
                0,
                10000
        );
        refreshAnimator.setDuration(REFRESH_ROTATE_DURATION);
        refreshAnimator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        refreshAnimator.setInterpolator(new LinearInterpolator());
        refreshAnimator.addUpdateListener(animation -> refreshFab.invalidate());
        refreshAnimator.start();
    }

    // 停止刷新动画（保证至少转一小会）
    private void stopRefreshAnim(@Nullable FloatingActionButton refreshFab) {
        long elapsed = System.currentTimeMillis() - refreshAnimStartTime;
        long delay = Math.max(0, MIN_REFRESH_ANIM_DURATION - elapsed);

        if (refreshFab == null) {
            if (refreshAnimator != null) {
                refreshAnimator.cancel();
                refreshAnimator = null;
            }
            if (refreshRotateDrawable != null) {
                refreshRotateDrawable.setLevel(0);
            }
            return;
        }

        refreshFab.postDelayed(() -> {
            if (refreshAnimator != null) {
                refreshAnimator.cancel();
                refreshAnimator = null;
            }
            if (refreshRotateDrawable != null) {
                refreshRotateDrawable.setLevel(0);
            }
            refreshFab.invalidate();
        }, delay);
    }

    private void showEmpty(boolean empty) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // 点击搜索后隐藏搜索框和键盘
    private void hideKeyboardAndClearFocus(EditText et) {
        et.clearFocus();

        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) requireContext()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }
}