package com.eightbitlab.blurview_sample.PatientDetail;

import com.google.gson.annotations.SerializedName;

public class VisitSimpleModel {
    @SerializedName(value = "patientId", alternate = {"PatientId"})
    public String patientId;
    @SerializedName(value = "visitNo", alternate = {"VisitNo"})
    public String visitNo;
    @SerializedName(value = "visitTime", alternate = {"VisitTime"})
    public String visitTime;//医疗事件发生的真实时间，医生看诊、病例归属的那一天，可选
    @SerializedName(value = "chiefComplaint", alternate = {"ChiefComplaint"})
    public String chiefComplaint;
    @SerializedName(value = "createTime", alternate = {"CreateTime"})
    public String createTime;
    @SerializedName(value = "updateTime", alternate = {"UpdateTime"})
    public String updateTime;

    // 你说的 IsNeedXXX 先不管，可以不写，也可以留着（不影响解析）
    // public boolean isNeedReport;
}
