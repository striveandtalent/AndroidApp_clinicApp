package com.eightbitlab.blurview_sample.VisitDetail;

import com.google.gson.annotations.SerializedName;

public class VisitTreatmentRecordItemModel {
    @SerializedName(value = "id", alternate = {"Id"})
    public long id;

    @SerializedName(value = "visitNo", alternate = {"VisitNo"})
    public String visitNo;

    @SerializedName(value = "recordType", alternate = {"RecordType"})
    public int recordType;   // 1初诊 2复诊

    @SerializedName(value = "recordTime", alternate = {"RecordTime"})
    public String recordTime;

    @SerializedName(value = "content", alternate = {"Content"})
    public String content;

    @SerializedName(value = "fee", alternate = {"Fee"})
    public double fee;

    @SerializedName(value = "createTime", alternate = {"CreateTime"})
    public String createTime;

    @SerializedName(value = "updateTime", alternate = {"UpdateTime"})
    public String updateTime;
}
