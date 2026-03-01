package com.eightbitlab.blurview_sample;

import com.google.gson.annotations.SerializedName;

public class ReturnInfo<T> {
    @SerializedName(value = "status", alternate = {"Status"})
    public int status;
    @SerializedName(value = "code", alternate = {"Code"})
    public String code;
  @SerializedName(value = "message", alternate = {"Message"})
    public String message;
   @SerializedName(value = "traceId", alternate = {"TraceId", "traceID", "TraceID"})
    public String traceId;
    @SerializedName(value = "data", alternate = {"Data"})
    public T data;
}
