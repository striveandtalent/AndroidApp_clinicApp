package com.eightbitlab.blurview_sample.VisitDetail;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VisitDetailAggregateModel {
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

    @SerializedName(value = "physicalSigns", alternate = {"PhysicalSigns"})
    public String physicalSigns;

    @SerializedName(value = "diagnosis", alternate = {"Diagnosis"})
    public String diagnosis;

    @SerializedName(value = "treatmentEffect", alternate = {"TreatmentEffect"})
    public Integer treatmentEffect;

    @SerializedName(value = "totalFee", alternate = {"TotalFee"})
    public double totalFee;

    @SerializedName(value = "doctorAdvice", alternate = {"DoctorAdvice"})
    public String doctorAdvice;

    @SerializedName(value = "remark", alternate = {"Remark"})
    public String remark;

    @SerializedName(value = "createTime", alternate = {"CreateTime"})
    public String createTime;

    @SerializedName(value = "updateTime", alternate = {"UpdateTime"})
    public String updateTime;

    @SerializedName(value = "treatmentRecords", alternate = {"TreatmentRecords"})
    public List<VisitTreatmentRecordItemModel> treatmentRecords;

    @SerializedName(value = "reports", alternate = {"Reports"})
    public List<VisitMediaItemModel> reports;

    @SerializedName(value = "prescriptions", alternate = {"Prescriptions"})
    public List<VisitMediaItemModel> prescriptions;

    @SerializedName(value = "otherAttachments", alternate = {"OtherAttachments"})
    public List<VisitMediaItemModel> otherAttachments;
}
