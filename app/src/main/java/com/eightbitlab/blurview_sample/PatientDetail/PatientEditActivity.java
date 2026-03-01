package com.eightbitlab.blurview_sample.PatientDetail;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.net.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientEditActivity extends AppCompatActivity {
    private String patientId;

    private EditText etName,etGender,etAge,etBirthday, etPhone,etIdCard, etAddress, etAllergy, etMedicalHistory, etMasterPlan;//需要更新档案的字段
    private Button btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_edit);

        patientId = getIntent().getStringExtra("patientId");

        etName = findViewById(R.id.etName);
        etGender = findViewById(R.id.etGender);
        etAge = findViewById(R.id.etAge);
        etBirthday = findViewById(R.id.etBirthday);
        etPhone = findViewById(R.id.etPhone);
        etIdCard = findViewById(R.id.etIdCard);
        etAddress = findViewById(R.id.etAddress);
        etAllergy = findViewById(R.id.etAllergy);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        etMasterPlan = findViewById(R.id.etMasterPlan);


        btnSave = findViewById(R.id.btnSave);

        loadPatientDetail(patientId);

        btnSave.setOnClickListener(v -> save());
    }

    //档案编辑更新页也要重新拉取最新的数据
    private void loadPatientDetail(String patientId) {
        ApiClient.api().getByPatientId(patientId).enqueue(new Callback<ReturnInfo<PatientModel>>() {
            @Override
            public void onResponse(Call<ReturnInfo<PatientModel>> call, Response<ReturnInfo<PatientModel>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().status != 0 || resp.body().data == null) {
                    Toast.makeText(PatientEditActivity.this, "获取档案失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                PatientModel p = resp.body().data;

                //加载数据
                etName.setText(nvl(p.name));
                etGender.setText(nvl(p.gender));
                etAge.setText(nvl(p.age));
                etBirthday.setText(nvl(p.birthday));
                etPhone.setText(nvl(p.phone));
                etIdCard.setText(nvl(p.idCard));
                etAddress.setText(nvl(p.address));
                etAllergy.setText(nvl(p.allergy));
                etMedicalHistory.setText(nvl(p.medicalHistory));
                etMasterPlan.setText(nvl(p.masterPlan));
            }

            @Override
            public void onFailure(Call<ReturnInfo<PatientModel>> call, Throwable t) {
                Toast.makeText(PatientEditActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        PatientModel req = new PatientModel();
        req.name = etName.getText().toString().trim();
        req.gender = etGender.getText().toString().trim();
        req.age = etAge.getText().toString().trim();
        req.birthday = etBirthday.getText().toString().trim();
        req.phone = etPhone.getText().toString().trim();
        req.idCard = etIdCard.getText().toString().trim();
        req.address = etAddress.getText().toString().trim();
        req.allergy = etAllergy.getText().toString().trim();
        req.medicalHistory = etMedicalHistory.getText().toString().trim();
        req.masterPlan = etMasterPlan.getText().toString().trim();

        ApiClient.api().updatePatient(patientId, req).enqueue(new Callback<ReturnInfo<Object>>() {
            @Override
            public void onResponse(Call<ReturnInfo<Object>> call, Response<ReturnInfo<Object>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().status != 0) {
                    Toast.makeText(PatientEditActivity.this, resp.body().message, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(PatientEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                // 告诉详情页刷新（方式一：setResult）
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(Call<ReturnInfo<Object>> call, Throwable t) {
                Toast.makeText(PatientEditActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
