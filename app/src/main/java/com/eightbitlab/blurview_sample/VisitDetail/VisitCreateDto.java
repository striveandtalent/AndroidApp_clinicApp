package com.eightbitlab.blurview_sample.VisitDetail;

import com.google.gson.annotations.SerializedName;

public class VisitCreateDto {

    @SerializedName(value = "patientId", alternate = {"PatientId"})
    public String patientId;

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

    @SerializedName(value = "doctorAdvice", alternate = {"DoctorAdvice"})
    public String doctorAdvice;

    @SerializedName(value = "remark", alternate = {"Remark"})
    public String remark;
}