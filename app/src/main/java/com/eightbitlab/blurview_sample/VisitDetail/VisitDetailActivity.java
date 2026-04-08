package com.eightbitlab.blurview_sample.VisitDetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.eightbitlab.blurview_sample.util.FileUriUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitDetailActivity extends AppCompatActivity {

    public static final String EXTRA_VISIT_NO = "visitNo";
    public static final String EXTRA_SECTION = "section";
    public static final String EXTRA_PATIENT_ID = "patientId";

    public static final int SECTION_ILLNESS = 1;
    public static final int SECTION_TREATMENT_PROCESS = 2;
    public static final int SECTION_DOCTOR_ADVICE = 3;
    public static final int SECTION_REMARK = 4;

    private static final int RECORD_TYPE_INITIAL = 1;
    private static final int RECORD_TYPE_FOLLOWUP = 2;

    private String visitNo;
    private String patientId;

    private TextView tvVisitNo;
    private TextView tvVisitTime;

    private TextView tvChiefComplaint;
    private TextView tvPresentIllness;
    private TextView tvPhysicalSigns;
    private TextView tvDiagnosis;
    private TextView tvReportAttachmentSummary;
    private GridLayout gridReportAttachments;
    private LinearLayout layoutReportFileFallback;

    private TextView tvInitialAction;
    private LinearLayout layoutInitialContainer;
    private TextView tvFollowupAction;
    private LinearLayout layoutFollowupContainer;

    private TextView tvPrescriptionAttachmentSummary;
    private GridLayout gridPrescriptionAttachments;
    private LinearLayout layoutPrescriptionFileFallback;
    private TextView tvTreatmentEffect;
    private TextView tvTotalFee;

    private TextView tvOtherAttachmentSummary;
    private GridLayout gridOtherAttachments;
    private LinearLayout layoutOtherFileFallback;

    private TextView tvDoctorAdvice;
    private TextView tvRemark;

    private ActivityResultLauncher<Intent> editLauncher;
    private ActivityResultLauncher<String> attachmentPickerLauncher;
    private int pendingMediaType = 0;

    private ActivityResultLauncher<Intent> treatmentRecordEditLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_detail);

        bindViews();

        visitNo = getIntent().getStringExtra(EXTRA_VISIT_NO);
        patientId = getIntent().getStringExtra(EXTRA_PATIENT_ID);

        if (isBlank(visitNo)) {
            Toast.makeText(this, "visitNo 为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        visitNo = visitNo.trim();

        if (!isBlank(patientId)) {
            patientId = patientId.trim();
        }

        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadVisitDetail(visitNo);
                    }
                }
        );
        treatmentRecordEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadVisitDetail(visitNo);
                    }
                }
        );

        attachmentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    uploadAttachment(uri, pendingMediaType);
                }
        );

        findViewById(R.id.cardIllness).setOnClickListener(v -> openEdit(SECTION_ILLNESS));
        findViewById(R.id.cardDoctorAdvice).setOnClickListener(v -> openEdit(SECTION_DOCTOR_ADVICE));
        findViewById(R.id.cardRemark).setOnClickListener(v -> openEdit(SECTION_REMARK));
        tvTreatmentEffect.setOnClickListener(v -> showTreatmentEffectDialog());
        loadVisitDetail(visitNo);
    }

    private void bindViews() {
        tvVisitNo = findViewById(R.id.tvVisitNo);
        tvVisitTime = findViewById(R.id.tvVisitTime);

        tvChiefComplaint = findViewById(R.id.tvChiefComplaint);
        tvPresentIllness = findViewById(R.id.tvPresentIllness);
        tvPhysicalSigns = findViewById(R.id.tvPhysicalSigns);
        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        tvReportAttachmentSummary = findViewById(R.id.tvReportAttachmentSummary);
        gridReportAttachments = findViewById(R.id.gridReportAttachments);
        layoutReportFileFallback = findViewById(R.id.layoutReportFileFallback);

        tvInitialAction = findViewById(R.id.tvInitialAction);
        layoutInitialContainer = findViewById(R.id.layoutInitialContainer);
        tvFollowupAction = findViewById(R.id.tvFollowupAction);
        layoutFollowupContainer = findViewById(R.id.layoutFollowupContainer);

        tvPrescriptionAttachmentSummary = findViewById(R.id.tvPrescriptionAttachmentSummary);
        gridPrescriptionAttachments = findViewById(R.id.gridPrescriptionAttachments);
        layoutPrescriptionFileFallback = findViewById(R.id.layoutPrescriptionFileFallback);
        tvTreatmentEffect = findViewById(R.id.tvTreatmentEffect);
        tvTotalFee = findViewById(R.id.tvTotalFee);

        tvOtherAttachmentSummary = findViewById(R.id.tvOtherAttachmentSummary);
        gridOtherAttachments = findViewById(R.id.gridOtherAttachments);
        layoutOtherFileFallback = findViewById(R.id.layoutOtherFileFallback);

        tvDoctorAdvice = findViewById(R.id.tvDoctorAdvice);
        tvRemark = findViewById(R.id.tvRemark);
    }

    private void openEdit(int section) {
        Intent it = new Intent(this, VisitEditActivity.class);
        it.putExtra(EXTRA_VISIT_NO, visitNo);
        it.putExtra(EXTRA_SECTION, section);
        if (!isBlank(patientId)) {
            it.putExtra(EXTRA_PATIENT_ID, patientId);
        }
        editLauncher.launch(it);
    }

    private void loadVisitDetail(String visitNo) {
        ApiClient.api().getVisitDetailAggregate(visitNo)
                .enqueue(new Callback<ReturnInfo<VisitDetailAggregateModel>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<VisitDetailAggregateModel>> call,
                                           @NonNull Response<ReturnInfo<VisitDetailAggregateModel>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(VisitDetailActivity.this,
                                    "HTTP错误：" + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ReturnInfo<VisitDetailAggregateModel> body = response.body();
                        if (body.status != 0 || body.data == null) {
                            Toast.makeText(VisitDetailActivity.this,
                                    isBlank(body.message) ? "获取失败" : body.message,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        bind(body.data);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<VisitDetailAggregateModel>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(VisitDetailActivity.this,
                                "网络异常：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bind(VisitDetailAggregateModel d) {
        tvVisitNo.setText("病历号： " + safe(d.visitNo));
        tvVisitTime.setText("就诊时间： " + showOrEmpty(fmtMaybeDateTime(d.visitTime)));

        tvChiefComplaint.setText(showOrEmpty(d.chiefComplaint));
        tvPresentIllness.setText(showOrEmpty(d.presentIllness));
        tvPhysicalSigns.setText(showOrEmpty(d.physicalSigns));
        tvDiagnosis.setText(showOrEmpty(d.diagnosis));

        tvReportAttachmentSummary.setText(buildReportAttachmentSummary(d));
        tvPrescriptionAttachmentSummary.setText(buildPrescriptionAttachmentSummary(d));
        tvOtherAttachmentSummary.setText(buildOtherAttachmentSummary(d));

        bindAttachmentSection(gridReportAttachments, layoutReportFileFallback, d.reports, 1);
        bindAttachmentSection(gridPrescriptionAttachments, layoutPrescriptionFileFallback, d.prescriptions, 2);
        bindAttachmentSection(gridOtherAttachments, layoutOtherFileFallback, d.otherAttachments, 3);

        bindTreatmentRecords(d.treatmentRecords);

        tvTreatmentEffect.setText(showOrEmpty(formatTreatmentEffect(d.treatmentEffect)));
        tvTotalFee.setText(showOrEmpty(formatTotalFee(d.totalFee)));

        tvDoctorAdvice.setText(showOrEmpty(d.doctorAdvice));
        tvRemark.setText(showOrEmpty(d.remark));
    }

    private void bindTreatmentRecords(List<VisitTreatmentRecordItemModel> records) {
        layoutInitialContainer.removeAllViews();
        layoutFollowupContainer.removeAllViews();

        VisitTreatmentRecordItemModel initialRecord = null;
        List<VisitTreatmentRecordItemModel> followups = new ArrayList<>();

        if (records != null) {
            for (VisitTreatmentRecordItemModel item : records) {
                if (item == null) continue;

                if (item.recordType == RECORD_TYPE_INITIAL) {
                    if (initialRecord == null) {
                        initialRecord = item;
                    } else {
                        // 理论上只有一条初诊；若后端重复，保留较早的一条更贴近“初诊”语义
                        if (compareDate(item.recordTime, initialRecord.recordTime) < 0) {
                            initialRecord = item;
                        }
                    }
                } else if (item.recordType == RECORD_TYPE_FOLLOWUP) {
                    followups.add(item);
                }
            }
        }

        if (initialRecord == null) {
            tvInitialAction.setText("+ 新增初诊");
            layoutInitialContainer.addView(createEmptyHint("暂无初诊记录"));
        } else {
            tvInitialAction.setText("编辑");
            String title = buildInitialTitle(initialRecord);
            layoutInitialContainer.addView(createTreatmentRecordCard(title, initialRecord, false));
        }

        final VisitTreatmentRecordItemModel finalInitialRecord = initialRecord;

        tvInitialAction.setOnClickListener(v -> {
            if (finalInitialRecord == null) {
                openCreateInitialRecord();
            } else {
                openEditInitialRecord(finalInitialRecord);
            }
        });

        tvFollowupAction.setText("+ 新增复诊");
        tvFollowupAction.setOnClickListener(v -> openCreateFollowupRecord());

        if (followups.isEmpty()) {
            layoutFollowupContainer.addView(createEmptyHint("暂无复诊记录"));
        } else {
            for (int i = 0; i < followups.size(); i++) {
                VisitTreatmentRecordItemModel item = followups.get(i);
                String title = buildFollowupTitle(item);
                layoutFollowupContainer.addView(createTreatmentRecordCard(title, item, true));
            }
        }
    }

    private String buildInitialTitle(VisitTreatmentRecordItemModel item) {
        String timeText = fmtMaybeDateTime(item == null ? null : item.recordTime);
        if (isBlank(timeText)) {
            return "初诊";
        }
        return "初诊 · " + timeText;
    }

    private String buildFollowupTitle(VisitTreatmentRecordItemModel item) {
        String timeText = fmtMaybeDateTime(item == null ? null : item.recordTime);
        if (isBlank(timeText)) {
            return "复诊";
        }
        return "复诊 · " + timeText;
    }

    private View createTreatmentRecordCard(String title,
                                           VisitTreatmentRecordItemModel item,
                                           boolean editableFollowup) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLp.topMargin = dp(8);
        card.setLayoutParams(cardLp);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundColor(0xFFF8FAFC);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvTitle.setTextColor(0xFF111827);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvTitle);



        TextView tvContent = new TextView(this);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contentLp.topMargin = dp(8);
        tvContent.setLayoutParams(contentLp);
        tvContent.setText(showOrEmpty(item == null ? null : item.content));
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvContent.setTextColor(0xFF111827);
        tvContent.setLineSpacing(0, 1.2f);
        card.addView(tvContent);

        TextView tvFee = new TextView(this);
        LinearLayout.LayoutParams feeLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        feeLp.topMargin = dp(10);
        tvFee.setLayoutParams(feeLp);
        tvFee.setText("单次金额： " + formatFee(item == null ? null : item.fee));
        tvFee.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvFee.setTextColor(0xFF2563EB);
        card.addView(tvFee);

        card.setOnClickListener(v -> {
            if (editableFollowup) {
                openEditFollowupRecord(item);
            } else {
                openEditInitialRecord(item);
            }
        });

        if (editableFollowup) {
            card.setOnLongClickListener(v -> {
                confirmDeleteTreatmentRecord(item);
                return true;
            });
        }

        return card;
    }

    private void confirmDeleteTreatmentRecord(VisitTreatmentRecordItemModel item) {
        if (item == null || item.id <= 0) {
            Toast.makeText(this, "复诊记录无效，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("删除复诊")
                .setMessage("确定删除这条复诊记录吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteTreatmentRecord(item))
                .show();
    }

    private void deleteTreatmentRecord(VisitTreatmentRecordItemModel item) {
        if (item == null || item.id <= 0) {
            Toast.makeText(this, "复诊记录无效，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBlank(visitNo)) {
            Toast.makeText(this, "visitNo 为空，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.api().deleteTreatmentRecord(item.id, visitNo)
                .enqueue(new Callback<ReturnInfo<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<Object>> call,
                                           @NonNull Response<ReturnInfo<Object>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(VisitDetailActivity.this,
                                    "删除失败：HTTP " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ReturnInfo<Object> body = response.body();
                        if (body.status != 0) {
                            Toast.makeText(VisitDetailActivity.this,
                                    isBlank(body.message) ? "删除失败" : body.message,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(VisitDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        loadVisitDetail(visitNo);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<Object>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(VisitDetailActivity.this,
                                "删除失败：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private View createEmptyHint(String text) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tv.setLayoutParams(lp);
        tv.setPadding(dp(14), dp(14), dp(14), dp(14));
        tv.setBackgroundColor(0xFFF8FAFC);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setTextColor(0xFF6B7280);
        return tv;
    }

    private int compareDate(String a, String b) {
        String aa = safe(a).trim();
        String bb = safe(b).trim();
        if (aa.equals(bb)) return 0;
        if (aa.isEmpty()) return 1;
        if (bb.isEmpty()) return -1;
        return aa.compareTo(bb);
    }

    private int compareDateDesc(String a, String b) {
        return -compareDate(a, b);
    }

    private String formatFee(Double fee) {
        if (fee == null || fee <= 0) {
            return "¥0.00";
        }
        return String.format(Locale.getDefault(), "¥%.2f", fee);
    }

    private void bindAttachmentSection(GridLayout grid, LinearLayout fallback, List<VisitMediaItemModel> list, int mediaType) {
        grid.removeAllViews();
        fallback.removeAllViews();

        if (list == null || list.isEmpty()) {
            addPlusPlaceholder(grid, mediaType);
            fallback.setVisibility(View.GONE);
            return;
        }

        boolean hasNonImage = false;

        for (VisitMediaItemModel item : list) {
            if (isImageAttachment(item)) {
                grid.addView(createImageThumb(list, item));
            } else {
                hasNonImage = true;
                fallback.addView(createFileFallbackCard(item));
            }
        }

        addPlusPlaceholder(grid, mediaType);
        fallback.setVisibility(hasNonImage ? View.VISIBLE : View.GONE);
    }

    private View createImageThumb(List<VisitMediaItemModel> sectionList, VisitMediaItemModel item) {
        int size = dp(64);

        ImageView iv = new ImageView(this);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = size;
        lp.height = size;
        lp.setMargins(0, 0, dp(6), dp(6));
        iv.setLayoutParams(lp);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(0xFFE5E7EB);
        iv.setPadding(dp(1), dp(1), dp(1), dp(1));

        String finalUrl = buildAbsoluteUrl(item.url);
        Glide.with(this)
                .load(finalUrl)
                .centerCrop()
                .into(iv);

        iv.setOnClickListener(v -> openImagePreview(sectionList, item));
        iv.setOnLongClickListener(v -> {
            confirmDelete(item);
            return true;
        });
        return iv;
    }

    private void openImagePreview(List<VisitMediaItemModel> sectionList, VisitMediaItemModel currentItem) {
        if (sectionList == null || sectionList.isEmpty() || currentItem == null) {
            Toast.makeText(this, "没有可预览的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> urlList = new ArrayList<>();
        ArrayList<String> fileNameList = new ArrayList<>();
        int currentIndex = 0;
        int imageIndex = 0;

        for (VisitMediaItemModel item : sectionList) {
            if (!isImageAttachment(item)) {
                continue;
            }

            String fullUrl = buildAbsoluteUrl(item.url);
            if (isBlank(fullUrl)) {
                continue;
            }

            urlList.add(fullUrl);
            fileNameList.add(safe(item.fileName));

            if (item == currentItem) {
                currentIndex = imageIndex;
            }
            imageIndex++;
        }

        if (urlList.isEmpty()) {
            Toast.makeText(this, "没有可预览的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(this, AttachmentPreviewActivity.class);
        it.putStringArrayListExtra(AttachmentPreviewActivity.EXTRA_URL_LIST, urlList);
        it.putStringArrayListExtra(AttachmentPreviewActivity.EXTRA_FILE_NAME_LIST, fileNameList);
        it.putExtra(AttachmentPreviewActivity.EXTRA_INDEX, currentIndex);
        startActivity(it);
    }

    private View createFileFallbackCard(VisitMediaItemModel item) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = dp(8);
        tv.setLayoutParams(lp);

        int p = dp(12);
        tv.setPadding(p, p, p, p);
        tv.setBackgroundColor(0xFFEEF2FF);
        tv.setTextColor(0xFF374151);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setLineSpacing(0, 1.2f);
        tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        tv.setText(buildAttachmentItemText(item));

        tv.setOnClickListener(v -> openAttachmentPreview(item));
        tv.setOnLongClickListener(v -> {
            confirmDelete(item);
            return true;
        });
        return tv;
    }

    private void confirmDelete(VisitMediaItemModel item) {
        if (item == null || item.id <= 0) {
            Toast.makeText(this, "附件记录无效，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("删除附件")
                .setMessage("确定删除这个附件吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteAttachment(item))
                .show();
    }

    private void addPlusPlaceholder(GridLayout grid, int mediaType) {
        TextView plus = new TextView(this);
        int size = dp(64);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = size;
        lp.height = size;
        lp.setMargins(0, 0, dp(6), dp(6));
        plus.setLayoutParams(lp);

        plus.setText("+");
        plus.setGravity(Gravity.CENTER);
        plus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        plus.setTextColor(0xFF111827);
        plus.setBackgroundColor(0xFFF9FAFB);
        plus.setPadding(dp(1), dp(1), dp(1), dp(1));

        plus.setOnClickListener(v -> {
            pendingMediaType = mediaType;
            attachmentPickerLauncher.launch("*/*");
        });

        grid.addView(plus);
    }

    private boolean isImageAttachment(VisitMediaItemModel item) {
        if (item == null) return false;

        String contentType = safe(item.contentType).toLowerCase();
        if (contentType.startsWith("image/")) {
            return true;
        }

        String ext = safe(item.ext).toLowerCase();
        return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png")
                || ext.equals(".webp") || ext.equals(".bmp");
    }
    private boolean isVideoAttachment(VisitMediaItemModel item) {
        if (item == null) return false;

        String contentType = safe(item.contentType).toLowerCase();
        if (contentType.startsWith("video/")) {
            return true;
        }

        String ext = safe(item.ext).toLowerCase();
        return ext.equals(".mp4")
                || ext.equals(".mov")
                || ext.equals(".m4v")
                || ext.equals(".3gp")
                || ext.equals(".webm");
    }
    private void openAttachmentPreview(VisitMediaItemModel item) {
        if (item == null || isBlank(item.url)) {
            Toast.makeText(this, "附件地址为空，无法预览", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalUrl = buildAbsoluteUrl(item.url);
        if (isBlank(finalUrl)) {
            Toast.makeText(this, "附件地址无效，无法预览", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isVideoAttachment(item)) {
            Intent it = new Intent(this, VideoPreviewActivity.class);
            it.putExtra("url", finalUrl);
            it.putExtra("fileName", safe(item.fileName));
            startActivity(it);
            return;
        }

        Intent it = new Intent(this, AttachmentPreviewActivity.class);
        it.putExtra(AttachmentPreviewActivity.EXTRA_URL, finalUrl);
        it.putExtra(AttachmentPreviewActivity.EXTRA_FILE_NAME, safe(item.fileName));
        it.putExtra(AttachmentPreviewActivity.EXTRA_CONTENT_TYPE, safe(item.contentType));
        startActivity(it);
    }

    private void uploadAttachment(Uri uri, int mediaType) {
        if (uri == null) return;

        if (isBlank(visitNo)) {
            Toast.makeText(this, "visitNo 为空，无法上传", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBlank(patientId)) {
            Toast.makeText(this, "patientId 为空，无法上传", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = FileUriUtil.getDisplayName(this, uri);
            if (isBlank(fileName)) {
                fileName = "upload_" + System.currentTimeMillis();
            }

            String mime = getContentResolver().getType(uri);
            if (isBlank(mime)) {
                mime = "application/octet-stream";
            }

            byte[] bytes = FileUriUtil.readBytes(this, uri);

            RequestBody fileBody = RequestBody.create(bytes, MediaType.parse(mime));
            MultipartBody.Part filePart =
                    MultipartBody.Part.createFormData("file", fileName, fileBody);

            RequestBody visitNoBody =
                    RequestBody.create(visitNo, MediaType.parse("text/plain"));
            RequestBody patientIdBody =
                    RequestBody.create(patientId, MediaType.parse("text/plain"));
            RequestBody mediaTypeBody =
                    RequestBody.create(String.valueOf(mediaType), MediaType.parse("text/plain"));

            ApiClient.api().uploadAttachment(filePart, visitNoBody, patientIdBody, mediaTypeBody)
                    .enqueue(new Callback<ReturnInfo<Object>>() {
                        @Override
                        public void onResponse(@NonNull Call<ReturnInfo<Object>> call,
                                               @NonNull Response<ReturnInfo<Object>> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(VisitDetailActivity.this,
                                        "上传失败：HTTP " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ReturnInfo<Object> body = response.body();
                            if (body.status != 0) {
                                Toast.makeText(VisitDetailActivity.this,
                                        isBlank(body.message) ? "上传失败" : body.message,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Toast.makeText(VisitDetailActivity.this, "上传成功", Toast.LENGTH_SHORT).show();

                            loadVisitDetail(visitNo);
                        }

                        @Override
                        public void onFailure(@NonNull Call<ReturnInfo<Object>> call,
                                              @NonNull Throwable t) {
                            Toast.makeText(VisitDetailActivity.this,
                                    "上传失败：" + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, "读取文件失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showTreatmentEffectDialog() {
        final String[] items = {"差", "一般", "好", "很好"};

        String currentText = tvTreatmentEffect.getText() == null ? "" : tvTreatmentEffect.getText().toString().trim();
        int checkedItem = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(currentText)) {
                checkedItem = i;
                break;
            }
        }

        final int[] selected = {checkedItem};

        new AlertDialog.Builder(this)
                .setTitle("选择治疗效果")
                .setSingleChoiceItems(items, checkedItem, (dialog, which) -> selected[0] = which)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    if (selected[0] < 0) {
                        Toast.makeText(this, "请选择治疗效果", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int effectValue = selected[0] + 1;
                    updateTreatmentEffect(effectValue);
                })
                .show();
    }

    private void updateTreatmentEffect(int treatmentEffect) {
        if (isBlank(visitNo)) {
            Toast.makeText(this, "visitNo 为空，无法更新", Toast.LENGTH_SHORT).show();
            return;
        }

        VisitUpdateRequest req = new VisitUpdateRequest();
        req.VisitNo = visitNo;
        req.TreatmentEffect = treatmentEffect;

        ApiClient.api().updateByVisitNo(req)
                .enqueue(new Callback<ReturnInfo<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<Object>> call,
                                           @NonNull Response<ReturnInfo<Object>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(VisitDetailActivity.this,
                                    "保存失败：HTTP " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ReturnInfo<Object> body = response.body();
                        if (body.status != 0) {
                            Toast.makeText(VisitDetailActivity.this,
                                    isBlank(body.message) ? "保存失败" : body.message,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(VisitDetailActivity.this, "治疗效果已更新", Toast.LENGTH_SHORT).show();
                        loadVisitDetail(visitNo);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<Object>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(VisitDetailActivity.this,
                                "保存失败：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String treatmentEffectText(int value) {
        switch (value) {
            case 1:
                return "差";
            case 2:
                return "一般";
            case 3:
                return "好";
            case 4:
                return "很好";
            default:
                return "（暂无）";
        }
    }

    private int treatmentEffectIndex(Integer value) {
        if (value == null || value < 1 || value > 4) return -1;
        return value - 1;
    }

    private void deleteAttachment(VisitMediaItemModel item) {
        if (item == null || item.id <= 0) {
            Toast.makeText(this, "附件记录无效，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.api().deleteAttachment(String.valueOf(item.id))
                .enqueue(new Callback<ReturnInfo<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ReturnInfo<Object>> call,
                                           @NonNull Response<ReturnInfo<Object>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(VisitDetailActivity.this,
                                    "删除失败：HTTP " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ReturnInfo<Object> body = response.body();
                        if (body.status != 0) {
                            Toast.makeText(VisitDetailActivity.this,
                                    isBlank(body.message) ? "删除失败" : body.message,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(VisitDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        loadVisitDetail(visitNo);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReturnInfo<Object>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(VisitDetailActivity.this,
                                "删除失败：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String buildAbsoluteUrl(String rawUrl) {
        String url = safe(rawUrl).trim();
        if (url.isEmpty()) return "";

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        String baseUrl = ApiClient.getBaseUrl(this);
        if (isBlank(baseUrl)) return url;

        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        if (url.startsWith("/")) {
            return base + url;
        }
        return base + "/" + url;
    }

    private String buildAttachmentItemText(VisitMediaItemModel item) {
        String fileName = showOrEmpty(safe(item.fileName));
        String ext = safe(item.ext);
        String contentType = safe(item.contentType);
        String sizeText = formatBytes(item.byteSize);

        StringBuilder sb = new StringBuilder();
        sb.append(fileName);

        if (!ext.isEmpty()) {
            sb.append("\n扩展名： ").append(ext);
        }

        if (!contentType.isEmpty()) {
            sb.append("\n类型： ").append(contentType);
        }

        if (!sizeText.isEmpty()) {
            sb.append("\n大小： ").append(sizeText);
        }

        return sb.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "";
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format(Locale.getDefault(), "%.2f MB", mb);
    }

    private String buildReportAttachmentSummary(VisitDetailAggregateModel d) {
        int count = sizeOf(d.reports);
        if (count <= 0) {
            return "暂无检查报告附件";
        }
        return "检查报告共 " + count + " 个";
    }

    private String buildPrescriptionAttachmentSummary(VisitDetailAggregateModel d) {
        int count = sizeOf(d.prescriptions);
        if (count <= 0) {
            return "暂无处方附件";
        }
        return "处方附件共 " + count + " 个";
    }

    private String buildOtherAttachmentSummary(VisitDetailAggregateModel d) {
        int count = sizeOf(d.otherAttachments);
        if (count <= 0) {
            return "暂无其他附件";
        }
        return "其他附件共 " + count + " 个";
    }

    private int sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private String formatTreatmentEffect(Integer effect) {
        if (effect == null || effect <= 0) return "";
        switch (effect) {
            case 1:
                return "差";
            case 2:
                return "一般";
            case 3:
                return "好";
            case 4:
                return "很好";
            default:
                return String.valueOf(effect);
        }
    }

    private String formatTotalFee(double totalFee) {
        if (totalFee <= 0) return "";
        return String.format(Locale.getDefault(), "¥%.2f", totalFee);
    }

    private String fmtMaybeDateTime(String s) {
        String v = safe(s).trim();
        if (v.isEmpty()) return "";
        v = v.replace("T", " ");
        int dot = v.indexOf('.');
        if (dot > 0) v = v.substring(0, dot);
        return v;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String showOrEmpty(String s) {
        String v = safe(s).trim();
        return v.isEmpty() ? "（暂无）" : v;
    }

    private void openCreateInitialRecord() {
        Intent it = new Intent(this, TreatmentRecordEditActivity.class);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_MODE, TreatmentRecordEditActivity.MODE_CREATE_INITIAL);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_VISIT_NO, visitNo);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_PATIENT_ID, patientId);
        treatmentRecordEditLauncher.launch(it);
    }

    private void openEditInitialRecord(VisitTreatmentRecordItemModel item) {
        if (item == null) return;

        Intent it = new Intent(this, TreatmentRecordEditActivity.class);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_MODE, TreatmentRecordEditActivity.MODE_EDIT_INITIAL);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_VISIT_NO, visitNo);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_PATIENT_ID, patientId);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_RECORD_ID, item.id);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_RECORD_TYPE, item.recordType);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_RECORD_TIME, item.recordTime);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_CONTENT, item.content);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_FEE, item.fee);
        treatmentRecordEditLauncher.launch(it);
    }

    private void openCreateFollowupRecord() {
        Intent it = new Intent(this, TreatmentRecordEditActivity.class);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_MODE, TreatmentRecordEditActivity.MODE_CREATE_FOLLOWUP);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_VISIT_NO, visitNo);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_PATIENT_ID, patientId);
        treatmentRecordEditLauncher.launch(it);
    }

    private void openEditFollowupRecord(VisitTreatmentRecordItemModel item) {
        if (item == null) return;

        Intent it = new Intent(this, TreatmentRecordEditActivity.class);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_MODE, TreatmentRecordEditActivity.MODE_EDIT_FOLLOWUP);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_VISIT_NO, visitNo);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_PATIENT_ID, patientId);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_RECORD_ID, item.id);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_RECORD_TYPE, item.recordType);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_RECORD_TIME, item.recordTime);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_CONTENT, item.content);
        it.putExtra(TreatmentRecordEditActivity.EXTRA_FEE, item.fee);
        treatmentRecordEditLauncher.launch(it);
    }
}