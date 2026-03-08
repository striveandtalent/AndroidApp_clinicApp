package com.eightbitlab.blurview_sample.PatientDetail;

import com.google.gson.annotations.SerializedName;

public class VisitSimpleModel {
    @SerializedName(value = "visitId", alternate = {"VisitId", "VisitID"})
    public String visitId;

    @SerializedName(value = "visitNo", alternate = {"VisitNo", "VisitNO"})
    public String visitNo;

    @SerializedName(value = "patientId", alternate = {"PatientId", "PatientID"})
    public String patientId;

    @SerializedName(value = "visitTime", alternate = {"VisitTime"})
    public String visitTime;

    @SerializedName(value = "chiefComplaint", alternate = {"ChiefComplaint"})
    public String chiefComplaint;

    @SerializedName(value = "presentIllness", alternate = {"PresentIllness"})
    public String presentIllness;

    @SerializedName(value = "createTime", alternate = {"CreateTime"})
    public String createTime;

    @SerializedName(value = "updateTime", alternate = {"UpdateTime"})
    public String updateTime;
}
