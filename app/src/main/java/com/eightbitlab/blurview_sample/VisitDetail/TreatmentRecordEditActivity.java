package com.eightbitlab.blurview_sample.VisitDetail;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.net.ApiClient;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TreatmentRecordEditActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_VISIT_NO = "visitNo";
    public static final String EXTRA_PATIENT_ID = "patientId";
    public static final String EXTRA_RECORD_ID = "recordId";
    public static final String EXTRA_RECORD_TYPE = "recordType";
    public static final String EXTRA_RECORD_TIME = "recordTime";
    public static final String EXTRA_CONTENT = "content";
    public static final String EXTRA_FEE = "fee";

    public static final int MODE_CREATE_INITIAL = 1;
    public static final int MODE_EDIT_INITIAL = 2;
    public static final int MODE_CREATE_FOLLOWUP = 3;
    public static final int MODE_EDIT_FOLLOWUP = 4;

    private TextView tvTitle;
    private EditText etRecordTime;
    private EditText etContent;
    private EditText etFee;
    private Button btnSave;

    private int mode;
    private String visitNo;
    private String patientId;
    private long recordId;
    private int recordType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_record_edit);

        tvTitle = findViewById(R.id.tvTitle);
        etRecordTime = findViewById(R.id.etRecordTime);
        etContent = findViewById(R.id.etContent);
        etFee = findViewById(R.id.etFee);
        btnSave = findViewById(R.id.btnSave);

        readIntent();
        bindTitle();
        bindInitialValues();

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        etRecordTime.setOnClickListener(v -> pickDateTime());
        etRecordTime.setFocusable(false);
        etRecordTime.setClickable(true);

        btnSave.setOnClickListener(v -> submit());
    }

    private void readIntent() {
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_CREATE_FOLLOWUP);
        visitNo = getIntent().getStringExtra(EXTRA_VISIT_NO);
        patientId = getIntent().getStringExtra(EXTRA_PATIENT_ID);
        recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, 0L);
        recordType = getIntent().getIntExtra(EXTRA_RECORD_TYPE, 0);
    }

    private void bindTitle() {
        switch (mode) {
            case MODE_CREATE_INITIAL:
                tvTitle.setText("新增初诊");
                recordType = 1;
                break;
            case MODE_EDIT_INITIAL:
                tvTitle.setText("编辑初诊");
                recordType = 1;
                break;
            case MODE_CREATE_FOLLOWUP:
                tvTitle.setText("新增复诊");
                recordType = 2;
                break;
            case MODE_EDIT_FOLLOWUP:
                tvTitle.setText("编辑复诊");
                recordType = 2;
                break;
            default:
                tvTitle.setText("编辑记录");
                break;
        }
    }

    private void bindInitialValues() {
        String time = getIntent().getStringExtra(EXTRA_RECORD_TIME);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        double fee = getIntent().getDoubleExtra(EXTRA_FEE, 0d);

        if (isBlank(time)) {
            etRecordTime.setText(nowText());
        } else {
            etRecordTime.setText(normalizeDateTime(time));
        }

        if (!isBlank(content)) {
            etContent.setText(content);
        }

        if (fee > 0) {
            etFee.setText(new DecimalFormat("0.##").format(fee));
        }
    }

    private void pickDateTime() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog tp = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                String text = String.format(
                                        Locale.getDefault(),
                                        "%04d-%02d-%02d %02d:%02d:00",
                                        year, month + 1, dayOfMonth, hourOfDay, minute
                                );
                                etRecordTime.setText(text);
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                    );
                    tp.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    private void submit() {
        String time = normalizeForApi(textOf(etRecordTime));
        String content = textOf(etContent);
        String feeText = textOf(etFee);

        if (isBlank(visitNo)) {
            toast("visitNo 为空");
            return;
        }

        if (isCreateMode() && isBlank(patientId)) {
            toast("patientId 为空");
            return;
        }

        if (isBlank(time)) {
            toast("请选择记录时间");
            return;
        }

        if (isBlank(content)) {
            toast("请输入内容");
            return;
        }

        double fee;
        try {
            fee = TextUtils.isEmpty(feeText) ? 0d : Double.parseDouble(feeText);
        } catch (Exception e) {
            toast("金额格式不正确");
            return;
        }

        if (fee < 0) {
            toast("金额不能小于 0");
            return;
        }

        if (isCreateMode()) {
            createRecord(time, content, fee);
        } else {
            updateRecord(time, content, fee);
        }
    }

    private String normalizeForApi(String s) {
        String v = s == null ? "" : s.trim();
        if (v.isEmpty()) return "";
        return v.replace(" ", "T");
    }

    private void createRecord(String time, String content, double fee) {
        TreatmentRecordCreateRequest req = new TreatmentRecordCreateRequest();
        req.visitNo = visitNo;
        req.patientId = patientId;
        req.recordType = recordType;
        req.recordTime = time;
        req.content = content;
        req.fee = fee;

        ApiClient.api().createTreatmentRecord(req)
                .enqueue(new Callback<ReturnInfo<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<Object>> call,
                                           @NonNull Response<ReturnInfo<Object>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            toast("保存失败：HTTP " + response.code());
                            return;
                        }

                        ReturnInfo<Object> body = response.body();
                        if (body.status != 0) {
                            toast(isBlank(body.message) ? "保存失败" : body.message);
                            return;
                        }

                        toast("保存成功");
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<Object>> call, @NonNull Throwable t) {
                        toast("保存失败：" + t.getMessage());
                    }
                });
    }

    private void updateRecord(String time, String content, double fee) {
        if (recordId <= 0) {
            toast("记录 Id 无效");
            return;
        }

        TreatmentRecordUpdateRequest req = new TreatmentRecordUpdateRequest();
        req.id = recordId;
        req.visitNo = visitNo;
        req.recordTime = time;
        req.content = content;
        req.fee = fee;

        ApiClient.api().updateTreatmentRecord(req)
                .enqueue(new Callback<ReturnInfo<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<Object>> call,
                                           @NonNull Response<ReturnInfo<Object>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            toast("更新失败：HTTP " + response.code());
                            return;
                        }

                        ReturnInfo<Object> body = response.body();
                        if (body.status != 0) {
                            toast(isBlank(body.message) ? "更新失败" : body.message);
                            return;
                        }

                        toast("更新成功");
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<Object>> call, @NonNull Throwable t) {
                        toast("更新失败：" + t.getMessage());
                    }
                });
    }

    private boolean isCreateMode() {
        return mode == MODE_CREATE_INITIAL || mode == MODE_CREATE_FOLLOWUP;
    }

    private String nowText() {
        Calendar c = Calendar.getInstance();
        return String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d %02d:%02d:00",
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE)
        );
    }

    private String normalizeDateTime(String s) {
        String v = s == null ? "" : s.trim();
        v = v.replace("T", " ");
        int dot = v.indexOf('.');
        if (dot > 0) v = v.substring(0, dot);
        return v;
    }

    private String textOf(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}