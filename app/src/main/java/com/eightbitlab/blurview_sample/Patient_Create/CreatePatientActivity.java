package com.eightbitlab.blurview_sample.Patient_Create;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.PatientDetail.PatientDetailActivity;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.ReturnInfo;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.DatePickerDialog;

import java.util.Calendar;
import java.util.Locale;

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
        //findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        setupBirthdayPicker();
    }

    private void setupBirthdayPicker() {
        etBirthday.setFocusable(false);
        etBirthday.setFocusableInTouchMode(false);
        etBirthday.setCursorVisible(false);
        etBirthday.setLongClickable(false);

        etBirthday.setOnClickListener(v -> showBirthdayPicker());
    }

    private void showBirthdayPicker() {
        final Calendar cal = Calendar.getInstance();

        // 如果已经有值（yyyy-MM-dd），就用它作为默认值
        String cur = etBirthday.getText().toString().trim();
        if (cur.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                int y = Integer.parseInt(cur.substring(0, 4));
                int m = Integer.parseInt(cur.substring(5, 7)) - 1;
                int d = Integer.parseInt(cur.substring(8, 10));
                cal.set(y, m, d);
            } catch (Exception ignored) {
            }
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    String value = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                    etBirthday.setText(value);
                },
                year, month, day
        );

        // 不允许选未来日期（建议）
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        dialog.show();
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

        String bd = etBirthday.getText().toString().trim();
        int t = bd.indexOf('T');
        if (t > 0) bd = bd.substring(0, t);
        req.birthday = bd.isEmpty() ? null : bd;

        //传年龄：
        String ageStr = etAge.getText().toString().trim();
        req.age = ageStr.isEmpty() ? null : Integer.parseInt(ageStr);

        req.phone = phone;
        req.idCard = etIdCard.getText().toString().trim();
        req.address = etAddress.getText().toString().trim();
        req.allergy = etAllergy.getText().toString().trim();
        req.medicalHistory = etMedicalHistory.getText().toString().trim();
        req.masterPlan = etMasterPlan.getText().toString().trim();


        ApiClient.api().createPatient(req).enqueue(new Callback<ReturnInfo<String>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<String>> call,
                                   @NonNull Response<ReturnInfo<String>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CreatePatientActivity.this, "保存失败：" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<String> body = response.body();
                if (body.status != 0) {
                    Toast.makeText(CreatePatientActivity.this, body.message == null ? "保存失败" : body.message, Toast.LENGTH_SHORT).show();
                    return;
                }

                String patientId = body.data;   //这里就是后端返回的 PatientId
                if (patientId == null || patientId.trim().isEmpty()) {
                    Toast.makeText(CreatePatientActivity.this, "创建成功但未返回 patientId", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent detail = new Intent(CreatePatientActivity.this, PatientDetailActivity.class);
                detail.putExtra("patientId", patientId);

                TaskStackBuilder.create(CreatePatientActivity.this)
                        .addNextIntentWithParentStack(detail)
                        .startActivities();

                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<String>> call, @NonNull Throwable t) {
                Toast.makeText(CreatePatientActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class CreatePatientRequest {
        public String name;
        public String gender;
        public String birthday;
        public Integer age;
        public String phone;
        public String idCard;
        public String address;
        public String allergy;//过敏史
        public String medicalHistory;//既往病史
        public String masterPlan;//总体治疗方针
    }
}