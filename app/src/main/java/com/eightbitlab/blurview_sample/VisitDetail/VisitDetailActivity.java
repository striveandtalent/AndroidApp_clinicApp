package com.eightbitlab.blurview_sample.VisitDetail;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.eightbitlab.blurview_sample.PatientDetail.VisitDetailModel;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.TimeFmt;
import com.eightbitlab.blurview_sample.net.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class VisitDetailActivity extends AppCompatActivity {

    private String visitNo;

    // Header
    private TextView tvVisitNo;
    private TextView tvVisitTime;

    // Section 1: 病情与方案
    private TextView tvChiefComplaint;
    private TextView tvPresentIllness;
    private TextView tvTreatmentPlan;

    // Section 2: 沟通与预后
    private TextView tvPatientCooperation;
    private TextView tvDoctorPatientRelation;
    private TextView tvPrognosisNote;

    // Section 3: 附件
    private TextView tvAttachmentEmpty;
    private FrameLayout flAttachmentContainer;

    // Section 4: 记录信息
    private TextView tvCreateTime;
    private TextView tvUpdateTime;

    //点击病历详情的卡片做跳转
    private ActivityResultLauncher<Intent> editLauncher;
    static final String EXTRA_VISIT_NO = "visitNo";
    static final String EXTRA_SECTION = "section";
    static final int SECTION_ILLNESS = 1;   // 病情与方案
    private static final int SECTION_RELATION = 2;  // 沟通与预后

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_detail);

        // Header
        tvVisitNo = findViewById(R.id.tvVisitNo);
        tvVisitTime = findViewById(R.id.tvVisitTime);

        // Section 1
        tvChiefComplaint = findViewById(R.id.tvChiefComplaint);
        tvPresentIllness = findViewById(R.id.tvPresentIllness);
        tvTreatmentPlan = findViewById(R.id.tvTreatmentPlan);

        // Section 2
        tvPatientCooperation = findViewById(R.id.tvPatientCooperation);
        tvDoctorPatientRelation = findViewById(R.id.tvDoctorPatientRelation);
        tvPrognosisNote = findViewById(R.id.tvPrognosisNote);

        // Section 3
        tvAttachmentEmpty = findViewById(R.id.tvAttachmentEmpty);
        flAttachmentContainer = findViewById(R.id.flAttachmentContainer);

        // Section 4
        tvCreateTime = findViewById(R.id.tvCreateTime);
        tvUpdateTime = findViewById(R.id.tvUpdateTime);

        visitNo = getIntent().getStringExtra("visitNo");
        if (visitNo == null || visitNo.trim().isEmpty()) {
            Toast.makeText(this, "visitNo 为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // 编辑保存成功后，回到详情页重新拉取刷新
                        loadVisitDetail(visitNo.trim());
                    }
                }
        );

        // 绑定卡片点击（XML 里 id 就是 cardIllness/cardRelation）
        findViewById(R.id.cardIllness).setOnClickListener(v -> openEdit(SECTION_ILLNESS));
        findViewById(R.id.cardRelation).setOnClickListener(v -> openEdit(SECTION_RELATION));

        loadVisitDetail(visitNo.trim());
    }
    private void openEdit(int section) {
        Intent it = new Intent(this, VisitEditActivity.class);
        it.putExtra(EXTRA_VISIT_NO, visitNo.trim());
        it.putExtra(EXTRA_SECTION, section);
        editLauncher.launch(it);
    }
    private void loadVisitDetail(String visitNo) {
        ApiClient.api().getVisitByVisitNo(visitNo).enqueue(new Callback<ReturnInfo<VisitDetailModel>>() {
            @Override
            public void onResponse(@NonNull Call<ReturnInfo<VisitDetailModel>> call,
                                   @NonNull Response<ReturnInfo<VisitDetailModel>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(VisitDetailActivity.this, "HTTP错误：" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                ReturnInfo<VisitDetailModel> body = response.body();
                if (body.status != 0 || body.data == null) {
                    Toast.makeText(VisitDetailActivity.this,
                            body.message == null ? "获取失败" : body.message,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                bind(body.data);
            }

            @Override
            public void onFailure(@NonNull Call<ReturnInfo<VisitDetailModel>> call, @NonNull Throwable t) {
                Toast.makeText(VisitDetailActivity.this, "网络异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bind(VisitDetailModel d) {
        // Header
        tvVisitNo.setText("病历号：" + safe(d.visitNo));
        tvVisitTime.setText("就诊时间：" + fmtTime(safe(d.visitTime)));

        // Section 1
        tvChiefComplaint.setText(showOrEmpty(d.chiefComplaint));
        tvPresentIllness.setText(showOrEmpty(d.presentIllness));
        tvTreatmentPlan.setText(showOrEmpty(d.treatmentPlan));

        // Section 2
        tvPatientCooperation.setText(showOrEmpty(d.patientCooperation));
        tvDoctorPatientRelation.setText(showOrEmpty(d.doctorPatientRelation));
        tvPrognosisNote.setText(showOrEmpty(d.prognosisNote));

        // Section 3: 附件（先占位）
        // 你后续如果有附件列表/图片URL，拿到后：
        // - tvAttachmentEmpty.setVisibility(View.GONE);
        // - 在 flAttachmentContainer 里 add RecyclerView / ImageView 等
        tvAttachmentEmpty.setVisibility(View.VISIBLE);
        flAttachmentContainer.setVisibility(View.VISIBLE);

        // Section 4
        tvCreateTime.setText("创建时间：" + fmtTime(safe(d.createTime)));
        tvUpdateTime.setText("最后更新时间：" + fmtTime(safe(d.updateTime)));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String showOrEmpty(String s) {
        String v = safe(s).trim();
        return v.isEmpty() ? "（暂无）" : v;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String fmtTime(String s) {
        // 你项目里 TimeFmt.fmt(...) 已经在列表里用过了:contentReference[oaicite:3]{index=3}
        // 这里做个保护：解析失败就原样显示
        try {
            return TimeFmt.fmtDateOnly(s);
        } catch (Exception e) {
            return s;
        }
    }
}