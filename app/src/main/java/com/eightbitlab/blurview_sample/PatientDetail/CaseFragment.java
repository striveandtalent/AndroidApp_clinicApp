package com.eightbitlab.blurview_sample.PatientDetail;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
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
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.Patient_Create.CreatePatientActivity;
import com.eightbitlab.blurview_sample.Patient_Search.PageResult;
import com.eightbitlab.blurview_sample.Patient_Search.PatientAdapter;

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

    public CaseFragment() {
        super(R.layout.fragment_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) BlurView 初始化（你原来的代码）
        BlurView topBlurView = view.findViewById(R.id.topBlurView);
        BlurTarget target = view.findViewById(R.id.caseBlurTarget);
        view.findViewById(R.id.fabAddPatient).setOnClickListener(v->{
            android.content.Intent it = new Intent(requireContext(), CreatePatientActivity.class);
            startActivity(it);
        });
        if (topBlurView == null || target == null) return;

        Drawable windowBackground = requireActivity().getWindow().getDecorView().getBackground();
        if (windowBackground == null) windowBackground = new ColorDrawable(Color.TRANSPARENT);

        topBlurView.setClipToOutline(true);
        topBlurView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view1, Outline outline) {
                if (view1.getBackground() != null) view1.getBackground().getOutline(outline);
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
            //做“详情页”就从这里跳转/或者再调 getByPatientId
            //Toast.makeText(requireContext(), "点击：" + item.name, Toast.LENGTH_SHORT).show();
            if (item == null || item.patientId == null || item.patientId.isEmpty()) return;
            Intent it = new Intent(requireContext(), PatientDetailActivity.class);
            it.putExtra("patientId", item.patientId);
            startActivity(it);
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // 3) 搜索框监听（点击键盘“搜索”触发）
        EditText searchInput = view.findViewById(R.id.searchInput);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearch = actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);

            if (isSearch) {
                String kw = v.getText() == null ? "" : v.getText().toString().trim();

                hideKeyboardAndClearFocus(searchInput);  //收键盘 + 去焦点
                doSearch(kw);

                return true;
            }
            return false;
        });

        // 可选：进入页面先拉一次（空条件=最近10条）
        doSearch("");
    }

    private void doSearch(String keyword) {
        // 你后端 Search 支持 Name/Phone/IDCard/... 多字段
        // 首页一个输入框：先按 Name 去搜（最常用）；后面你想做“智能判断手机号/身份证”也可以扩展
        Call<ReturnInfo<PageResult<PatientModel>>> call =
                ApiClient.api().searchPatients(
                        keyword,   // Name
                        null,      // Phone
                        null,      // IDCard
                        null,      // Address
                        null,      // Allergy
                        null       // MedicalHistory
                );

        call.enqueue(new Callback<ReturnInfo<PageResult<PatientModel>>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<PageResult<PatientModel>>> call,
                                   @NonNull Response<ReturnInfo<PageResult<PatientModel>>> response) {

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
                    showEmpty(true);
                    Toast.makeText(requireContext(), body.message == null ? "查询失败" : body.message, Toast.LENGTH_SHORT).show();
                    return;
                }
                //业务成功
                List<PatientModel> items = (body.data == null) ? null : body.data.items;
                if (items == null) {
                    adapter.submit(Collections.emptyList());
                    showEmpty(true);
                } else {
                    adapter.submit(items);
                    showEmpty(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<PageResult<PatientModel>>> call, @NonNull Throwable t) {
                adapter.submit(Collections.emptyList());
                showEmpty(true);
                Toast.makeText(requireContext(), "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmpty(boolean empty) {
        if (tvEmpty != null) tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }


    //点击搜索后隐藏搜索框和键盘方法
    private void hideKeyboardAndClearFocus(EditText et) {
        // 1) 清焦点（去掉光标）
        et.clearFocus();

        // 2) 隐藏键盘
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) requireContext()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }
}