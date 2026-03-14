package com.eightbitlab.blurview_sample.VisitDetail;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.net.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitCreateActivity extends AppCompatActivity {

    private String patientId;

    private EditText etChiefComplaint;
    private EditText etPresentIllness;
    private EditText etPhysicalSigns;
    private EditText etDiagnosis;
    private EditText etDoctorAdvice;
    private EditText etRemark;
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
        etPhysicalSigns = findViewById(R.id.etPhysicalSigns);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etDoctorAdvice = findViewById(R.id.etDoctorAdvice);
        etRemark = findViewById(R.id.etRemark);

        btnSaveVisit = findViewById(R.id.btnSaveVisit);
        btnSaveVisit.setOnClickListener(v -> doSave());
    }

    private void doSave() {
        String chiefComplaint = getText(etChiefComplaint);
        String presentIllness = getText(etPresentIllness);
        String physicalSigns = getText(etPhysicalSigns);
        String diagnosis = getText(etDiagnosis);
        String doctorAdvice = getText(etDoctorAdvice);
        String remark = getText(etRemark);

        if (chiefComplaint.isEmpty()) {
            Toast.makeText(this, "主诉不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        VisitCreateDto dto = new VisitCreateDto();
        dto.patientId = patientId;
        dto.visitTime = null;          // 先让后端用当前时间
        dto.chiefComplaint = chiefComplaint;
        dto.presentIllness = emptyToNull(presentIllness);
        dto.physicalSigns = emptyToNull(physicalSigns);
        dto.diagnosis = emptyToNull(diagnosis);
        dto.treatmentEffect = null;    // 创建页先不填，后续详情页再维护
        dto.doctorAdvice = emptyToNull(doctorAdvice);
        dto.remark = emptyToNull(remark);

        btnSaveVisit.setEnabled(false);

        ApiClient.api().createVisit(dto).enqueue(new Callback<ReturnInfo<String>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<String>> call,
                                   @NonNull Response<ReturnInfo<String>> response) {
                btnSaveVisit.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(VisitCreateActivity.this,
                            "HTTP错误：" + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<String> body = response.body();
                if (body.status != 0) {
                    Toast.makeText(VisitCreateActivity.this,
                            body.message == null ? "创建失败" : body.message,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(VisitCreateActivity.this, "病历创建成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<String>> call, @NonNull Throwable t) {
                btnSaveVisit.setEnabled(true);
                Toast.makeText(VisitCreateActivity.this,
                        "网络异常：" + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(EditText et) {
        return et == null ? "" : et.getText().toString().trim();
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}