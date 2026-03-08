package com.eightbitlab.blurview_sample.VisitDetail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.PatientDetail.PatientDetailActivity;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.VisitDetail.VisitCreateDto;
import com.eightbitlab.blurview_sample.net.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitCreateActivity extends AppCompatActivity {

    private String patientId;

    private EditText etChiefComplaint, etPresentIllness, etTreatmentPlan;
    private EditText etPatientCooperation, etDoctorPatientRelation, etPrognosisNote;
    private Button btnSaveVisit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_create);

        patientId = getIntent().getStringExtra("patientId");
        if (patientId == null || patientId.trim().isEmpty()) {
            Toast.makeText(this, "patientId 为空，无法创建病历", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etChiefComplaint = findViewById(R.id.etChiefComplaint);
        etPresentIllness = findViewById(R.id.etPresentIllness);
        etTreatmentPlan = findViewById(R.id.etTreatmentPlan);

        etPatientCooperation = findViewById(R.id.etPatientCooperation);
        etDoctorPatientRelation = findViewById(R.id.etDoctorPatientRelation);
        etPrognosisNote = findViewById(R.id.etPrognosisNote);

        btnSaveVisit = findViewById(R.id.btnSaveVisit);

        btnSaveVisit.setOnClickListener(v -> doSave());
    }

    private void doSave() {
        String chief = etChiefComplaint.getText().toString().trim();
        String present = etPresentIllness.getText().toString().trim();
        String plan = etTreatmentPlan.getText().toString().trim();

        String coop = etPatientCooperation.getText().toString().trim();
        String relation = etDoctorPatientRelation.getText().toString().trim();
        String prog = etPrognosisNote.getText().toString().trim();

        // 自己定必填项，我建议至少主诉必填
        if (chief.isEmpty()) {
            Toast.makeText(this, "主诉不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        VisitCreateDto dto = new VisitCreateDto();
        dto.patientId = patientId;
        dto.chiefComplaint = chief;
        dto.presentIllness = present;
        dto.treatmentPlan = plan;
        dto.patientCooperation = coop;
        dto.doctorPatientRelation = relation;
        dto.prognosisNote = prog;

        btnSaveVisit.setEnabled(false);

        ApiClient.api().createVisit(dto).enqueue(new Callback<ReturnInfo<String>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<String>> call,
                                   @NonNull Response<ReturnInfo<String>> response) {
                btnSaveVisit.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(VisitCreateActivity.this, "HTTP错误：" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<String> body = response.body();
                if (body.status != 0 || body.data == null || body.data.trim().isEmpty()) {
                    Toast.makeText(VisitCreateActivity.this,
                            body.message == null ? "创建失败" : body.message,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // body.data = visitNo
                Toast.makeText(VisitCreateActivity.this, "创建成功，病历号：" + body.data, Toast.LENGTH_SHORT).show();

                //创建后直接跳转到对应页面
                 setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<String>> call, @NonNull Throwable t) {
                btnSaveVisit.setEnabled(true);
                Toast.makeText(VisitCreateActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}