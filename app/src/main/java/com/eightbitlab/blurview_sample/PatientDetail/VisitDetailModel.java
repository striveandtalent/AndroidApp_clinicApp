package com.eightbitlab.blurview_sample.PatientDetail;

import com.google.gson.annotations.SerializedName;

public class VisitDetailModel {
    @SerializedName(value = "patientId", alternate = {"PatientId"})
    public String patientId;

    @SerializedName(value = "visitNo", alternate = {"VisitNo"})
    public String visitNo;

    @SerializedName(value = "visitTime", alternate = {"VisitTime"})
    public String visitTime;

    @SerializedName(value = "chiefComplaint", alternate = {"ChiefComplaint"})
    public String chiefComplaint;

    @SerializedName(value = "presentIllness", alternate = {"PresentIllness"})
    public String presentIllness;

    @SerializedName(value = "treatmentPlan", alternate = {"TreatmentPlan"})
    public String treatmentPlan;

    @SerializedName(value = "patientCooperation", alternate = {"PatientCooperation"})
    public String patientCooperation;

    @SerializedName(value = "doctorPatientRelation", alternate = {"DoctorPatientRelation"})
    public String doctorPatientRelation;

    @SerializedName(value = "prognosisNote", alternate = {"PrognosisNote"})
    public String prognosisNote;

    //暂时不用的字段
    /*@SerializedName(value = "isNeedReport", alternate = {"IsNeedReport"})
    public boolean isNeedReport;

    @SerializedName(value = "isNeedCT", alternate = {"IsNeedCT"})
    public boolean isNeedCT;

    @SerializedName(value = "isNeedDR", alternate = {"IsNeedDR"})
    public boolean isNeedDR;

    @SerializedName(value = "isNeedMR", alternate = {"IsNeedMR"})
    public boolean isNeedMR;

    @SerializedName(value = "isNeedVideo", alternate = {"IsNeedVideo"})
    public boolean isNeedVideo;

    @SerializedName(value = "isNeedPrescription", alternate = {"IsNeedPrescription"})
    public boolean isNeedPrescription;*/


    @SerializedName(value = "createTime", alternate = {"CreateTime"})
    public String createTime;

    @SerializedName(value = "updateTime", alternate = {"UpdateTime"})
    public String updateTime;
}
