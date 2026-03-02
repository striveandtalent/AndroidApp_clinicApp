package com.eightbitlab.blurview_sample.VisitDetail;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.PatientDetail.VisitDetailModel;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.net.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitEditActivity extends AppCompatActivity {

    private String visitNo;
    private int section;

    private LinearLayout groupIllness, groupRelation;
    private EditText etChiefComplaint, etPresentIllness, etTreatmentPlan;
    private EditText etPatientCooperation, etDoctorPatientRelation, etPrognosisNote;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_edit);

        visitNo = getIntent().getStringExtra(VisitDetailActivity.EXTRA_VISIT_NO);
        section = getIntent().getIntExtra(VisitDetailActivity.EXTRA_SECTION, VisitDetailActivity.SECTION_ILLNESS);

        groupIllness = findViewById(R.id.groupIllness);
        groupRelation = findViewById(R.id.groupRelation);

        etChiefComplaint = findViewById(R.id.etChiefComplaint);
        etPresentIllness = findViewById(R.id.etPresentIllness);
        etTreatmentPlan = findViewById(R.id.etTreatmentPlan);

        etPatientCooperation = findViewById(R.id.etPatientCooperation);
        etDoctorPatientRelation = findViewById(R.id.etDoctorPatientRelation);
        etPrognosisNote = findViewById(R.id.etPrognosisNote);

        btnSave = findViewById(R.id.btnSave);

        // 只显示对应组
        if (section == VisitDetailActivity.SECTION_ILLNESS) {
            groupIllness.setVisibility(View.VISIBLE);
            groupRelation.setVisibility(View.GONE);
            setTitle("编辑 - 病情与方案");
        } else {
            groupIllness.setVisibility(View.GONE);
            groupRelation.setVisibility(View.VISIBLE);
            setTitle("编辑 - 沟通与预后");
        }

        // （可选）为了体验更好：进来先拉一次详情，把现有内容填进输入框
        loadAndBind();

        btnSave.setOnClickListener(v -> submit());
    }

    private void loadAndBind() {
        ApiClient.api().getVisitByVisitNo(visitNo).enqueue(new Callback<ReturnInfo<VisitDetailModel>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<VisitDetailModel>> call,
                                   @NonNull Response<ReturnInfo<VisitDetailModel>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().status != 0 || resp.body().data == null) {
                    Toast.makeText(VisitEditActivity.this, "获取就诊详情失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                VisitDetailModel d = resp.body().data;

                if (section == VisitDetailActivity.SECTION_ILLNESS) {
                    etChiefComplaint.setText(nvl(d.chiefComplaint));
                    etPresentIllness.setText(nvl(d.presentIllness));
                    etTreatmentPlan.setText(nvl(d.treatmentPlan));
                } else {
                    etPatientCooperation.setText(nvl(d.patientCooperation));
                    etDoctorPatientRelation.setText(nvl(d.doctorPatientRelation));
                    etPrognosisNote.setText(nvl(d.prognosisNote));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<VisitDetailModel>> call, @NonNull Throwable t) {
                Toast.makeText(VisitEditActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private void submit() {
        VisitUpdateRequest req = new VisitUpdateRequest();
        req.VisitNo = visitNo;

        if (section == VisitDetailActivity.SECTION_ILLNESS) {
            req.ChiefComplaint = trimOrNull(etChiefComplaint);
            req.PresentIllness = trimOrNull(etPresentIllness);
            req.TreatmentPlan = trimOrNull(etTreatmentPlan);

            // 其他字段必须保持 null，避免你后端做全量覆盖（如果后端没做“有值才更新”，会被置空）
            req.PatientCooperation = null;
            req.DoctorPatientRelation = null;
            req.PrognosisNote = null;
        } else {
            req.PatientCooperation = trimOrNull(etPatientCooperation);
            req.DoctorPatientRelation = trimOrNull(etDoctorPatientRelation);
            req.PrognosisNote = trimOrNull(etPrognosisNote);

            req.ChiefComplaint = null;
            req.PresentIllness = null;
            req.TreatmentPlan = null;
        }

        ApiClient.api().updateByVisitNo(req).enqueue(new retrofit2.Callback<ReturnInfo>() {
            @Override
            public void onResponse(retrofit2.Call<ReturnInfo> call, retrofit2.Response<ReturnInfo> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().status == 0) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(VisitEditActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ReturnInfo> call, Throwable t) {
                Toast.makeText(VisitEditActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String trimOrNull(EditText et) {
        String s = et.getText().toString().trim();
        return s.isEmpty() ? null : s;
    }
}