package com.eightbitlab.blurview_sample.VisitDetail;

import com.google.gson.annotations.SerializedName;

public class VisitUpdateRequest {
    @SerializedName("VisitNo")
    public String VisitNo;

    @SerializedName("VisitTime")
    public String VisitTime; // 暂时不用可以不填

    @SerializedName("ChiefComplaint")
    public String ChiefComplaint;

    @SerializedName("PresentIllness")
    public String PresentIllness;

    @SerializedName("TreatmentPlan")
    public String TreatmentPlan;

    @SerializedName("PatientCooperation")
    public String PatientCooperation;

    @SerializedName("DoctorPatientRelation")
    public String DoctorPatientRelation;

    @SerializedName("PrognosisNote")
    public String PrognosisNote;
}
