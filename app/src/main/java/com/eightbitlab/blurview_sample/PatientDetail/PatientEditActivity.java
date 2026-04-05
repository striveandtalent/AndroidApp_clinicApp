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

import android.app.DatePickerDialog;

import java.util.Calendar;
import java.util.Locale;

import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PatientEditActivity extends AppCompatActivity {
    private String patientId;

    private EditText etName, etAge, etBirthday, etPhone, etIdCard, etAddress, etAllergy, etMedicalHistory, etMasterPlan;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_edit);

        patientId = getIntent().getStringExtra("patientId");

        etName = findViewById(R.id.etName);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        etAge = findViewById(R.id.etAge);

        etBirthday = findViewById(R.id.etBirthday);
        setupBirthdayPicker();

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

    private void setupBirthdayPicker() {
        // 禁止手输，只允许点选
        etBirthday.setFocusable(false);
        etBirthday.setFocusableInTouchMode(false);
        etBirthday.setCursorVisible(false);
        etBirthday.setLongClickable(false);

        etBirthday.setOnClickListener(v -> showBirthdayPicker());
    }

    private void showBirthdayPicker() {
        final Calendar cal = Calendar.getInstance();

        // 如果已经有值（yyyy-MM-dd），用它作为默认选中日期
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
                    // 回填 yyyy-MM-dd
                    String value = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                    etBirthday.setText(value);
                },
                year, month, day
        );

        // 限制不能选未来日期（可选，但很建议）
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        dialog.show();
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

                // 加载数据
                etName.setText(nvl(p.name));

                String gender = nvl(p.gender);
                if ("男".equals(gender)) {
                    rgGender.check(R.id.rbMale);
                } else if ("女".equals(gender)) {
                    rgGender.check(R.id.rbFemale);
                } else {
                    rgGender.clearCheck();
                }

                etAge.setText(p.age == null ? "" : String.valueOf(p.age));

                String bd = nvl(p.birthday);
                if (!bd.isEmpty()) {
                    int t = bd.indexOf('T');
                    if (t > 0) bd = bd.substring(0, t);
                }
                etBirthday.setText(bd);

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

        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMale) {
            req.gender = "男";
        } else if (checkedId == R.id.rbFemale) {
            req.gender = "女";
        } else {
            req.gender = "";
        }
        if (req.gender == null || req.gender.trim().isEmpty()) {
            Toast.makeText(PatientEditActivity.this, "请选择性别", Toast.LENGTH_SHORT).show();
            return;
        }
        String ageStr = etAge.getText().toString().trim();
        req.age = ageStr.isEmpty() ? null : Integer.parseInt(ageStr);

        String bd = etBirthday.getText().toString().trim();
        req.birthday = bd.isEmpty() ? null : bd;

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
                    String msg = (resp.body() != null && resp.body().message != null)
                            ? resp.body().message
                            : "保存失败";
                    Toast.makeText(PatientEditActivity.this, msg, Toast.LENGTH_SHORT).show();
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

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
