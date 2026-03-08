package com.eightbitlab.blurview_sample.VisitDetail;

public class VisitUpdateRequest {

    public String VisitNo;

    // 可空，先不在编辑页里改它
    public String VisitTime;

    public String ChiefComplaint;
    public String PresentIllness;
    public String PhysicalSigns;
    public String Diagnosis;

    // 后端是 int?
    public Integer TreatmentEffect;

    public String DoctorAdvice;
    public String Remark;
}