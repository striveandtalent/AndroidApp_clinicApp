package com.eightbitlab.blurview_sample.VisitDetail;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.net.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitEditActivity extends AppCompatActivity {

    private String visitNo;
    private int section;

    private LinearLayout groupIllness;
    private LinearLayout groupTreatmentProcess;
    private LinearLayout groupDoctorAdvice;
    private LinearLayout groupRemark;

    private EditText etChiefComplaint;
    private EditText etPresentIllness;
    private EditText etPhysicalSigns;
    private EditText etDiagnosis;

    private EditText etTreatmentEffect;
    private EditText etDoctorAdvice;
    private EditText etRemark;

    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_edit);

        visitNo = getIntent().getStringExtra(VisitDetailActivity.EXTRA_VISIT_NO);
        section = getIntent().getIntExtra(
                VisitDetailActivity.EXTRA_SECTION,
                VisitDetailActivity.SECTION_ILLNESS
        );

        if (isBlank(visitNo)) {
            Toast.makeText(this, "visitNo 为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        visitNo = visitNo.trim();

        bindViews();
        initSectionUi();
        loadAndBind();

        btnSave.setOnClickListener(v -> submit());
    }

    private void bindViews() {
        groupIllness = findViewById(R.id.groupIllness);
        groupTreatmentProcess = findViewById(R.id.groupTreatmentProcess);
        groupDoctorAdvice = findViewById(R.id.groupDoctorAdvice);
        groupRemark = findViewById(R.id.groupRemark);

        etChiefComplaint = findViewById(R.id.etChiefComplaint);
        etPresentIllness = findViewById(R.id.etPresentIllness);
        etPhysicalSigns = findViewById(R.id.etPhysicalSigns);
        etDiagnosis = findViewById(R.id.etDiagnosis);

        etTreatmentEffect = findViewById(R.id.etTreatmentEffect);
        etDoctorAdvice = findViewById(R.id.etDoctorAdvice);
        etRemark = findViewById(R.id.etRemark);

        btnSave = findViewById(R.id.btnSave);
    }

    private void initSectionUi() {
        groupIllness.setVisibility(View.GONE);
        groupTreatmentProcess.setVisibility(View.GONE);
        groupDoctorAdvice.setVisibility(View.GONE);
        groupRemark.setVisibility(View.GONE);

        switch (section) {
            case VisitDetailActivity.SECTION_ILLNESS:
                groupIllness.setVisibility(View.VISIBLE);
                setTitle("编辑 - 病情与方案");
                break;
            case VisitDetailActivity.SECTION_TREATMENT_PROCESS:
                groupTreatmentProcess.setVisibility(View.VISIBLE);
                setTitle("编辑 - 治疗经过");
                break;
            case VisitDetailActivity.SECTION_DOCTOR_ADVICE:
                groupDoctorAdvice.setVisibility(View.VISIBLE);
                setTitle("编辑 - 医嘱");
                break;
            case VisitDetailActivity.SECTION_REMARK:
                groupRemark.setVisibility(View.VISIBLE);
                setTitle("编辑 - 备注");
                break;
            default:
                groupIllness.setVisibility(View.VISIBLE);
                setTitle("编辑 - 病情与方案");
                break;
        }
    }

    private void loadAndBind() {
        ApiClient.api().getVisitDetailAggregate(visitNo)
                .enqueue(new Callback<ReturnInfo<VisitDetailAggregateModel>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<VisitDetailAggregateModel>> call,
                                           @NonNull Response<ReturnInfo<VisitDetailAggregateModel>> resp) {
                        if (!resp.isSuccessful() || resp.body() == null || resp.body().status != 0 || resp.body().data == null) {
                            Toast.makeText(VisitEditActivity.this, "获取就诊详情失败", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        VisitDetailAggregateModel d = resp.body().data;

                        switch (section) {
                            case VisitDetailActivity.SECTION_ILLNESS:
                                etChiefComplaint.setText(nvl(d.chiefComplaint));
                                etPresentIllness.setText(nvl(d.presentIllness));
                                etPhysicalSigns.setText(nvl(d.physicalSigns));
                                etDiagnosis.setText(nvl(d.diagnosis));
                                break;
                            case VisitDetailActivity.SECTION_TREATMENT_PROCESS:
                                etTreatmentEffect.setText(d.treatmentEffect == null ? "" : String.valueOf(d.treatmentEffect));
                                break;
                            case VisitDetailActivity.SECTION_DOCTOR_ADVICE:
                                etDoctorAdvice.setText(nvl(d.doctorAdvice));
                                break;
                            case VisitDetailActivity.SECTION_REMARK:
                                etRemark.setText(nvl(d.remark));
                                break;
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<VisitDetailAggregateModel>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(VisitEditActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submit() {
        VisitUpdateRequest req = new VisitUpdateRequest();
        req.VisitNo = visitNo;
        req.VisitTime = null;

        req.ChiefComplaint = null;
        req.PresentIllness = null;
        req.PhysicalSigns = null;
        req.Diagnosis = null;
        req.TreatmentEffect = null;
        req.DoctorAdvice = null;
        req.Remark = null;

        switch (section) {
            case VisitDetailActivity.SECTION_ILLNESS:
                req.ChiefComplaint = trimOrNull(etChiefComplaint);
                req.PresentIllness = trimOrNull(etPresentIllness);
                req.PhysicalSigns = trimOrNull(etPhysicalSigns);
                req.Diagnosis = trimOrNull(etDiagnosis);
                break;
            case VisitDetailActivity.SECTION_TREATMENT_PROCESS:
                req.TreatmentEffect = parseNullableInt(etTreatmentEffect);
                break;
            case VisitDetailActivity.SECTION_DOCTOR_ADVICE:
                req.DoctorAdvice = trimOrNull(etDoctorAdvice);
                break;
            case VisitDetailActivity.SECTION_REMARK:
                req.Remark = trimOrNull(etRemark);
                break;
        }

        btnSave.setEnabled(false);

        ApiClient.api().updateByVisitNo(req).enqueue(new Callback<ReturnInfo<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<Object>> call,
                                   @NonNull Response<ReturnInfo<Object>> resp) {
                btnSave.setEnabled(true);

                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(VisitEditActivity.this,
                            "保存失败：" + resp.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<Object> body = resp.body();
                if (body.status == 0) {
                    Toast.makeText(VisitEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(VisitEditActivity.this,
                            isBlank(body.message) ? "保存失败" : body.message,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<Object>> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(VisitEditActivity.this,
                        "网络异常：" + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer parseNullableInt(EditText et) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            Toast.makeText(this, "治疗效果必须是数字", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String trimOrNull(EditText et) {
        String s = et.getText().toString().trim();
        return s.isEmpty() ? null : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}