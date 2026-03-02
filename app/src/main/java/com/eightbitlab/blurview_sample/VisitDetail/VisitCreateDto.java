package com.eightbitlab.blurview_sample.VisitDetail;

import com.google.gson.annotations.SerializedName;

public class VisitCreateDto {
    @SerializedName(value = "patientId", alternate = {"PatientId"})
    public String patientId;

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
}
