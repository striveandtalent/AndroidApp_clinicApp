package com.eightbitlab.blurview_sample.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.MainActivity;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.net.AppSettings;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etPassword;
    private Button btnLogin;

    private CheckBox cbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String token = TokenManager.getToken(this);

        if(token != null && !token.isEmpty()){

            startActivity(
                    new Intent(this, MainActivity.class)
            );

            finish();

            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        cbRemember = findViewById(R.id.cbRemember);

        btnLogin.setOnClickListener(v -> {

            String userName =
                    etUserName.getText().toString().trim();

            String password =
                    etPassword.getText().toString().trim();

            if(userName.isEmpty() || password.isEmpty()){

                Toast.makeText(
                        this,
                        "请输入账号密码",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            doLogin(userName,password);
        });

        ImageView ivSettings =
                findViewById(R.id.ivSettings);

        ivSettings.setOnClickListener(v -> {
            showEnvDialog();
        });
    }

    private void showEnvDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_env, null);

        RadioButton rbLocal =
                view.findViewById(R.id.rbLocal);

        RadioButton rbOnline =
                view.findViewById(R.id.rbOnline);

        EditText etLocal =
                view.findViewById(R.id.etLocal);

        EditText etOnline =
                view.findViewById(R.id.etOnline);

        // 读取保存的地址
        String localUrl =
                AppSettings.getBaseUrlByEnv(
                        this,
                        AppSettings.ENV_LOCAL
                );

        String onlineUrl =
                AppSettings.getBaseUrlByEnv(
                        this,
                        AppSettings.ENV_PUBLIC
                );

        etLocal.setText(localUrl);
        etOnline.setText(onlineUrl);

        // 当前环境
        String currentEnv =
                AppSettings.getCurrentEnv(this);

        if(currentEnv.equals(AppSettings.ENV_LOCAL)){

            rbLocal.setChecked(true);

        }else{

            rbOnline.setChecked(true);
        }

        builder.setView(view);

        builder.setPositiveButton("保存",
                (dialog, which) -> {

                    String local =
                            etLocal.getText()
                                    .toString()
                                    .trim();

                    String online =
                            etOnline.getText()
                                    .toString()
                                    .trim();

                    // 保存两个环境地址
                    AppSettings.setBaseUrlByEnv(
                            this,
                            AppSettings.ENV_LOCAL,
                            local
                    );

                    AppSettings.setBaseUrlByEnv(
                            this,
                            AppSettings.ENV_PUBLIC,
                            online
                    );

                    // 保存当前环境
                    if(rbLocal.isChecked()){

                        AppSettings.setCurrentEnv(
                                this,
                                AppSettings.ENV_LOCAL
                        );

                    }else{

                        AppSettings.setCurrentEnv(
                                this,
                                AppSettings.ENV_PUBLIC
                        );
                    }

                    // 重建 Retrofit
                    ApiClient.reset(this);

                    Toast.makeText(
                            this,
                            "环境已切换",
                            Toast.LENGTH_SHORT
                    ).show();
                });

        builder.setNegativeButton("取消", null);

        builder.show();
    }

    private void doLogin(String userName,String password) {

        LoginRequest req = new LoginRequest();

        req.userName = userName;
        req.password = password;

        ApiClient.api(this)
                .login(req)
                .enqueue(new Callback<ReturnInfo<LoginResponse>>() {

                    @Override
                    public void onResponse(
                            Call<ReturnInfo<LoginResponse>> call,
                            Response<ReturnInfo<LoginResponse>> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "登录失败",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        ReturnInfo<LoginResponse> result =
                                response.body();

                        if (result.status != 0) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    result.code,
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        LoginResponse data = result.data;


                        // 根据勾选状态决定是否记住Token登录
                        if (cbRemember.isChecked()) {

                            // 永久保存
                            TokenManager.saveToken(
                                    LoginActivity.this,
                                    data.token
                            );

                        } else {

                            // 仅本次运行有效
                            TokenManager.saveMemoryToken(
                                    data.token
                            );
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                "登录成功",
                                Toast.LENGTH_SHORT
                        ).show();

                        // 进入主页
                        startActivity(
                                new Intent(
                                        LoginActivity.this,
                                        MainActivity.class
                                )
                        );

                        finish();
                    }

                    @Override
                    public void onFailure(
                            Call<ReturnInfo<LoginResponse>> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                LoginActivity.this,
                                "网络异常：" + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
