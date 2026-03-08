package com.eightbitlab.blurview_sample.PatientDetail;

import android.content.Intent;
import android.os.Build;
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
import com.eightbitlab.blurview_sample.util.TimeFmt;
import com.eightbitlab.blurview_sample.VisitDetail.VisitCreateActivity;
import com.eightbitlab.blurview_sample.VisitDetail.VisitDetailActivity;
import com.eightbitlab.blurview_sample.net.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDetailActivity extends AppCompatActivity {

    private RecyclerView rvVisitRecords;
    private View layoutVisitRecordsEmpty;
    private VisitCardAdapter visitAdapter;

    private TextView tvName;
    private TextView tvGender;
    private TextView tvAge;
    private TextView tvBirthday;
    private TextView tvPhone;
    private TextView tvIdCard;
    private TextView tvAddress;
    private TextView tvAllergy;
    private TextView tvMedicalHistory;
    private TextView tvMasterPlan;
    private TextView tvPatientId;

    private String patientId;

    private ActivityResultLauncher<Intent> editPatientLauncher;
    private ActivityResultLauncher<Intent> createVisitLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        bindViews();

        patientId = getIntent().getStringExtra("patientId");
        if (isBlank(patientId)) {
            Toast.makeText(this, "patientId 为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        patientId = patientId.trim();

        registerLaunchers();
        setupPatientCardClick();
        setupVisitList();
        setupAddVisitButton();

        loadAll();
    }

    private void bindViews() {
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

        rvVisitRecords = findViewById(R.id.rvVisitRecords);
        layoutVisitRecordsEmpty = findViewById(R.id.layoutVisitVisitEmpty);
    }

    private void registerLaunchers() {
        editPatientLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadAll();
                    }
                }
        );

        createVisitLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadVisits(patientId);
                    }
                }
        );
    }

    private void setupPatientCardClick() {
        findViewById(R.id.cardPatientInfo).setOnClickListener(v -> {
            Intent it = new Intent(PatientDetailActivity.this, PatientEditActivity.class);
            it.putExtra("patientId", patientId);
            editPatientLauncher.launch(it);
        });
    }

    private void setupAddVisitButton() {
        findViewById(R.id.btnAddVisitFab).setOnClickListener(v -> {
            Intent it = new Intent(PatientDetailActivity.this, VisitCreateActivity.class);
            it.putExtra("patientId", patientId);
            createVisitLauncher.launch(it);
        });
    }

    private void setupVisitList() {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setAutoMeasureEnabled(true);
        rvVisitRecords.setLayoutManager(lm);
        rvVisitRecords.setHasFixedSize(false);
        rvVisitRecords.setNestedScrollingEnabled(false);

        visitAdapter = new VisitCardAdapter(item -> {
            if (item == null || isBlank(item.visitNo)) {
                Toast.makeText(PatientDetailActivity.this, "该病历缺少 visitNo，无法打开详情", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent it = new Intent(PatientDetailActivity.this, VisitDetailActivity.class);
            it.putExtra("visitNo", item.visitNo.trim());
            it.putExtra("patientId", patientId);
            startActivity(it);
        });

        rvVisitRecords.setAdapter(visitAdapter);
        renderVisitEmpty(true);
    }

    private void loadAll() {
        loadDetail(patientId);
        loadVisits(patientId);
    }

    private void loadVisits(String patientId) {
        ApiClient.api().getVisitsByPatientId(patientId)
                .enqueue(new Callback<ReturnInfo<List<VisitSimpleModel>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<List<VisitSimpleModel>>> call,
                                           @NonNull Response<ReturnInfo<List<VisitSimpleModel>>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            renderVisitEmpty(true);
                            Toast.makeText(PatientDetailActivity.this,
                                    "病历获取失败：" + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ReturnInfo<List<VisitSimpleModel>> body = response.body();
                        if (body.status != 0) {
                            renderVisitEmpty(true);
                            Toast.makeText(PatientDetailActivity.this,
                                    safe(body.message, "病历获取失败"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<VisitSimpleModel> data = body.data;
                        if (data == null) {
                            data = new ArrayList<>();
                        }

                        visitAdapter.submit(data);
                        renderVisitEmpty(data.isEmpty());

                        // 这一句是为了帮助你当前排查
                        Toast.makeText(PatientDetailActivity.this,
                                "病历条数：" + data.size(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<List<VisitSimpleModel>>> call,
                                          @NonNull Throwable t) {
                        renderVisitEmpty(true);
                        Toast.makeText(PatientDetailActivity.this,
                                "病历网络异常：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderVisitEmpty(boolean empty) {
        layoutVisitRecordsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvVisitRecords.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void loadDetail(String patientId) {
        ApiClient.api().getByPatientId(patientId)
                .enqueue(new Callback<ReturnInfo<PatientModel>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<PatientModel>> call,
                                           @NonNull Response<ReturnInfo<PatientModel>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(PatientDetailActivity.this,
                                    "HTTP错误：" + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ReturnInfo<PatientModel> body = response.body();
                        if (body.status != 0) {
                            Toast.makeText(PatientDetailActivity.this,
                                    safe(body.message, "获取失败"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        PatientModel p = body.data;
                        if (p == null) {
                            Toast.makeText(PatientDetailActivity.this,
                                    "返回 data 为空",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        bindPatient(p);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<PatientModel>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(PatientDetailActivity.this,
                                "网络异常：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindPatient(PatientModel p) {
        tvPatientId.setText(safe(p.patientId));
        tvName.setText(safe(p.name));
        tvGender.setText(safe(p.gender));
        tvAge.setText(safe(String.valueOf(p.age)));

        String birthday = safe(String.valueOf(p.birthday));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !birthday.isEmpty()) {
            try {
                tvBirthday.setText(TimeFmt.fmtDateOnly(birthday));
            } catch (Exception e) {
                tvBirthday.setText(birthday);
            }
        } else {
            tvBirthday.setText(birthday);
        }

        tvPhone.setText(safe(p.phone));
        tvIdCard.setText(safe(p.idCard));
        tvAddress.setText(safe(p.address));
        tvAllergy.setText(safe(p.allergy));
        tvMedicalHistory.setText(safe(p.medicalHistory));
        tvMasterPlan.setText(safe(p.masterPlan));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }
}