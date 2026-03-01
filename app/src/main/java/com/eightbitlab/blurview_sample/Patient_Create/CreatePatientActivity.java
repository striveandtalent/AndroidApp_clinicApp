package com.eightbitlab.blurview_sample.Patient_Create;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.ReturnInfo;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePatientActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etGender;
    private EditText etBirthday;
    private EditText etAge;
    private EditText etPhone;
    private EditText etIdCard;
    private EditText etAddress;
    private EditText etAllergy;
    private EditText etMedicalHistory;
    private EditText etMasterPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_patient);

        etName = findViewById(R.id.etName);
        etGender = findViewById(R.id.etGender);
        etBirthday = findViewById(R.id.etBirthday);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etIdCard = findViewById(R.id.etIdCard);
        etAddress = findViewById(R.id.etAddress);
        etAllergy = findViewById(R.id.etAllergy);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        etMasterPlan = findViewById(R.id.etMasterPlan);

        findViewById(R.id.btnSave).setOnClickListener(v -> submit());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void submit() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO：根据你后端 Create DTO 实际字段来
        CreatePatientRequest req = new CreatePatientRequest();
        req.name = name;
        req.gender = etGender.getText().toString().trim();
        //req.birthday= (Date)etBirthday;
        //req.Age = etAge;
        req.phone = phone;
        req.idCard = etIdCard.getText().toString().trim();
        req.address = etAddress.getText().toString().trim();
        req.allergy = etAllergy.getText().toString().trim();
        req.medicalHistory = etMedicalHistory.getText().toString().trim();
        req.masterPlan = etMasterPlan.getText().toString().trim();


        ApiClient.api().createPatient(req).enqueue(new Callback<ReturnInfo<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<Object>> call, @NonNull Response<ReturnInfo<Object>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CreatePatientActivity.this, "保存失败：" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                ReturnInfo<Object> body = response.body();
                if (body.status != 0) {
                    Toast.makeText(CreatePatientActivity.this, body.message == null ? "保存失败" : body.message, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(CreatePatientActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<Object>> call, @NonNull Throwable t) {
                Toast.makeText(CreatePatientActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class CreatePatientRequest {
        public String name;
        public String gender;
        public Date birthday;
        public int Age;
        public String phone;
        public String idCard;
        public String address;
        public String allergy;//过敏史
        public String medicalHistory;//既往病史
        public String masterPlan;//总体治疗方针
    }
}