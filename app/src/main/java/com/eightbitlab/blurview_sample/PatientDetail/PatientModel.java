package com.eightbitlab.blurview_sample.PatientDetail;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class PatientModel {

    @SerializedName(value = "patientId", alternate = {"PatientId", "PatientID"})
    public String patientId;

    @SerializedName(value = "name", alternate = {"Name"})
    public String name;

    @SerializedName(value = "gender", alternate = {"Gender"})
    public String gender;
    @SerializedName(value = "age", alternate = {"Age"})
    public Integer age;
    @SerializedName(value = "birthday", alternate = {"Birthday"})
    public String birthday;
    @SerializedName(value = "phone", alternate = {"Phone"})
    public String phone;

    @SerializedName(value = "idCard", alternate = {"IDCard", "IdCard"})
    public String idCard;

    @SerializedName(value = "address", alternate = {"Address"})
    public String address;

    @SerializedName(value = "allergy", alternate = {"Allergy"})
    public String allergy;
    @SerializedName(value = "medicalHistory", alternate = {"MedicalHistory"})
    public String medicalHistory;
    @SerializedName(value = "masterPlan", alternate = {"MasterPlan"})
    public String masterPlan;

    @SerializedName(value = "createTime", alternate = {"CreateTime"})
    public String createTime;

    @SerializedName(value = "updateTime", alternate = {"UpdateTime"})
    public String updateTime;


    /*public String patientId;
    public String name;
    public String gender;
    public Date birthDay;
    public int age;
    public String phone;
    public String idCard;
    public String address;
    public String allergy;//过敏史（如青霉素过敏）
    public String MedicalHistory;//既往病史（如高血压、糖尿病）
    public String MasterPlan;//总体诊疗方针
    public Date CreateTime;
    public Date UpdateTime;*/

}
