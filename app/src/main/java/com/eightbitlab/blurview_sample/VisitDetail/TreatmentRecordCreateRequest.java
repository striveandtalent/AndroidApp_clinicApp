package com.eightbitlab.blurview_sample.VisitDetail;

public class TreatmentRecordCreateRequest {
    public String visitNo;
    public String patientId;
    public int recordType;   // 1=初诊 2=复诊
    public String recordTime;
    public String content;
    public double fee;
}