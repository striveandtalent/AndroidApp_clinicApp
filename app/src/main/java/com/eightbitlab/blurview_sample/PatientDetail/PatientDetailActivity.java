package com.eightbitlab.blurview_sample.PatientDetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.TimeFmt;
import com.eightbitlab.blurview_sample.VisitDetail.VisitDetailActivity;
import com.eightbitlab.blurview_sample.net.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDetailActivity extends AppCompatActivity {

    private RecyclerView rvVisitRecords;
    private View layoutVisitRecordsEmpty;
    private VisitCardAdapter visitAdapter;

    private TextView tvName, tvGender, tvAge, tvBirthday, tvPhone, tvIdCard, tvAddress, tvAllergy, tvMedicalHistory, tvMasterPlan, tvPatientId;

    private static final int REQ_EDIT_PATIENT = 1001;//对档案编辑页面的请求码
    private String patientId; // 把 patientId 提到成员变量，方便 onActivityResult 用
    private ActivityResultLauncher<Intent> editPatientLauncher;//接收档案编辑页返回数据用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //档案字段
        setContentView(R.layout.activity_patient_detail);
        tvName = findViewById(R.id.tvName);
        tvGender = findViewById(R.id.tvGender);
        tvAge = findViewById(R.id.tvAge);
        tvBirthday = findViewById(R.id.tvBirthday);
        tvPhone = findViewById(R.id.tvPhone);
        tvIdCard = findViewById(R.id.tvIdCard);
        tvAddress = findViewById(R.id.tvAddress);
        tvAllergy = findViewById(R.id.tvAllergy);
        tvMedicalHistory = findViewById(R.id.tvMedicalHistory);
        tvMasterPlan = findViewById(R.id.tvMasterPlan);
        tvPatientId = findViewById(R.id.tvPatientId);


        patientId = getIntent().getStringExtra("patientId");
        if (patientId == null || patientId.trim().isEmpty()) {
            Toast.makeText(this, "patientId 为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDetail(patientId.trim());

        //添加点击档案卡片跳转到更新档案页面
        editPatientLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadDetail(patientId.trim()); // 刷新档案
                    }
                }
        );

        findViewById(R.id.cardPatientInfo).setOnClickListener(v -> {
            Intent it = new Intent(PatientDetailActivity.this, PatientEditActivity.class);
            it.putExtra("patientId", patientId.trim());
            editPatientLauncher.launch(it);
        });

        //病历字段
        rvVisitRecords = findViewById(R.id.rvVisitRecords);
        layoutVisitRecordsEmpty = findViewById(R.id.layoutVisitVisitEmpty);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setAutoMeasureEnabled(true);
        rvVisitRecords.setLayoutManager(lm);
        rvVisitRecords.setHasFixedSize(false);          // 很关键：不要固定尺寸
        rvVisitRecords.setNestedScrollingEnabled(false);// 你 XML 里写了，这里再保险

        //获取病历列表并交给 Adapter
        visitAdapter = new VisitCardAdapter(item -> {
            Intent it = new Intent(PatientDetailActivity.this, VisitDetailActivity.class);
            it.putExtra("visitNo", item.visitNo);
            it.putExtra("patientId", item.patientId);
            startActivity(it);
        });
        rvVisitRecords.setAdapter(visitAdapter);
        String pid = patientId.trim();
        loadDetail(pid);
        loadVisits(pid);
    }

    private void loadVisits(String patientId) {
        ApiClient.api().getVisitsByPatientId(patientId).enqueue(new Callback<ReturnInfo<List<VisitSimpleModel>>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<List<VisitSimpleModel>>> call,
                                   @NonNull Response<ReturnInfo<List<VisitSimpleModel>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    renderVisitEmpty(true);
                    Toast.makeText(PatientDetailActivity.this, "病历获取失败：" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<List<VisitSimpleModel>> body = response.body();
                if (body.status != 0 || body.data == null) {
                    renderVisitEmpty(true);
                    Toast.makeText(PatientDetailActivity.this,
                            body.message == null ? "病历获取失败" : body.message,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                visitAdapter.submit(body.data);
                renderVisitEmpty(body.data.isEmpty());
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<List<VisitSimpleModel>>> call, @NonNull Throwable t) {
                renderVisitEmpty(true);
                Toast.makeText(PatientDetailActivity.this, "病历网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderVisitEmpty(boolean empty) {
        layoutVisitRecordsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvVisitRecords.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void loadDetail(String patientId) {
        ApiClient.api().getByPatientId(patientId).enqueue(new Callback<ReturnInfo<PatientModel>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<PatientModel>> call,
                                   @NonNull Response<ReturnInfo<PatientModel>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(PatientDetailActivity.this, "HTTP错误：" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<PatientModel> body = response.body();
                if (body.status != 0) {
                    Toast.makeText(PatientDetailActivity.this,
                            body.message == null ? "获取失败" : body.message,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                PatientModel p = body.data;
                if (p == null) {
                    Toast.makeText(PatientDetailActivity.this, "返回 data 为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 渲染
                tvPatientId.setText(safe(p.patientId));
                tvName.setText(safe(p.name));
                tvGender.setText(safe(p.gender));
                tvAge.setText(safe(p.age));
                tvBirthday.setText(TimeFmt.fmtDateOnly(safe(p.birthday)));
                tvPhone.setText(safe(p.phone));
                tvIdCard.setText(safe(p.idCard));
                tvAddress.setText(safe(p.address));
                tvAllergy.setText(safe(p.allergy));
                tvMedicalHistory.setText(safe(p.medicalHistory));
                tvMasterPlan.setText(safe(p.masterPlan));
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<PatientModel>> call, @NonNull Throwable t) {
                Toast.makeText(PatientDetailActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}