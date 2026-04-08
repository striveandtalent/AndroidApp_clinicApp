package com.eightbitlab.blurview_sample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.net.AppSettings;

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
        if (getContext() == null) return;

        try {
            ApiClient.reset(requireContext());
            tvConnectionState.setText("已应用");
            Toast.makeText(requireContext(),
                    "当前地址：" + AppSettings.getBaseUrl(requireContext()),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            tvConnectionState.setText("异常");
            Toast.makeText(requireContext(), "连接配置失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }
}