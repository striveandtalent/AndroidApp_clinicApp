package com.eightbitlab.blurview_sample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.net.AppSettings;

import com.eightbitlab.blurview_sample.ReturnInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends BaseFragment {

    private LinearLayout itemServerAddress;
    private LinearLayout itemEnvironment;
    private LinearLayout itemTestConnection;

    private TextView tvServerAddressValue;
    private TextView tvEnvironmentValue;
    private TextView tvConnectionState;

    @Override
    int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemServerAddress = view.findViewById(R.id.itemServerAddress);
        itemEnvironment = view.findViewById(R.id.itemEnvironment);
        itemTestConnection = view.findViewById(R.id.itemTestConnection);

        tvServerAddressValue = view.findViewById(R.id.tvServerAddressValue);
        tvEnvironmentValue = view.findViewById(R.id.tvEnvironmentValue);
        tvConnectionState = view.findViewById(R.id.tvConnectionState);

        bindData();
        bindEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    private void bindData() {
        tvServerAddressValue.setText(AppSettings.getBaseUrl(requireContext()));
        tvEnvironmentValue.setText(AppSettings.getCurrentEnvDisplayName(requireContext()));
        tvConnectionState.setText("未检测");
    }

    private void bindEvents() {
        itemServerAddress.setOnClickListener(v -> showEditBaseUrlDialog());
        itemEnvironment.setOnClickListener(v -> showEnvironmentDialog());
        itemTestConnection.setOnClickListener(v -> testConnection());
    }

    private void showEditBaseUrlDialog() {
        final EditText editText = new EditText(requireContext());
        editText.setSingleLine(true);
        editText.setText(AppSettings.getBaseUrl(requireContext()));
        editText.setSelection(editText.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle("编辑当前环境服务器地址")
                .setView(editText)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String input = editText.getText() == null ? "" : editText.getText().toString().trim();

                    if (!AppSettings.isValidBaseUrl(input)) {
                        Toast.makeText(requireContext(), "服务器地址格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 只改当前环境对应的地址
                    AppSettings.setBaseUrl(requireContext(), input);

                    // 重建 Retrofit
                    ApiClient.reset(requireContext());

                    bindData();

                    Toast.makeText(requireContext(),
                            "已保存到当前环境：" + AppSettings.getCurrentEnvDisplayName(requireContext()),
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showEnvironmentDialog() {
        final String[] names = new String[]{
                "本地",
                "线上"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("切换环境")
                .setItems(names, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            applyEnvironment(AppSettings.ENV_LOCAL);
                            break;
                        case 1:
                            applyEnvironment(AppSettings.ENV_PUBLIC);
                            break;
                    }
                })
                .show();
    }

    private void applyEnvironment(String env) {
        AppSettings.setCurrentEnv(requireContext(), env);
        ApiClient.reset(requireContext());
        bindData();
        Toast.makeText(requireContext(),
                "已切换到：" + AppSettings.getCurrentEnvDisplayName(requireContext()),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 基础版本：
     * 当前先验证 ApiClient 是否已经按新的 baseUrl 重建成功
     */
    private void testConnection() {
        if (!isAdded() || getContext() == null) return;

        itemTestConnection.setEnabled(false);
        tvConnectionState.setText("检测中...");

        ApiClient.reset(requireContext());

        final long startMs = System.currentTimeMillis();

        ApiClient.api(requireContext()).ping().enqueue(new Callback<ReturnInfo<Object>>() {
            @Override
            public void onResponse(Call<ReturnInfo<Object>> call, Response<ReturnInfo<Object>> response) {
                if (!isAdded() || getContext() == null) return;

                itemTestConnection.setEnabled(true);

                long costMs = System.currentTimeMillis() - startMs;

                if (response.isSuccessful() && response.body() != null) {
                    ReturnInfo<Object> body = response.body();

                    if (body.status == 0) {
                        String msg = "连接正常 · " + costMs + "ms";
                        tvConnectionState.setText(msg);

//                        Toast.makeText(
//                                requireContext(),
//                                AppSettings.getCurrentEnvDisplayName(requireContext()) + "：" + msg,
//                                Toast.LENGTH_SHORT
//                        ).show();
                    } else {
                        String msg = "服务返回异常";
                        if (body.message != null && !body.message.trim().isEmpty()) {
                            msg = body.message.trim();
                        }

                        tvConnectionState.setText("连接异常");
//                        Toast.makeText(
//                                requireContext(),
//                                "连接异常：" + msg,
//                                Toast.LENGTH_SHORT
//                        ).show();
                    }
                } else {
                    tvConnectionState.setText("连接失败");
//                    Toast.makeText(
//                            requireContext(),
//                            "连接失败：HTTP " + response.code(),
//                            Toast.LENGTH_SHORT
//                    ).show();
                }
            }

            @Override
            public void onFailure(Call<ReturnInfo<Object>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;

                itemTestConnection.setEnabled(true);

                String errorMsg = (t == null) ? "未知错误" : t.getMessage();
                if (errorMsg == null || errorMsg.trim().isEmpty()) {
                    errorMsg = "网络请求失败";
                }

                if (errorMsg.toLowerCase().contains("timeout")) {
                    tvConnectionState.setText("连接超时");
                } else {
                    tvConnectionState.setText("连接失败");
                }

                Toast.makeText(
                        requireContext(),
                        "连接失败：" + errorMsg,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private int dp(int value) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }
}