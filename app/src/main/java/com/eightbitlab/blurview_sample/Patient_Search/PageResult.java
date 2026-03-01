package com.eightbitlab.blurview_sample.Patient_Search;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PageResult<T> {
    @SerializedName(value = "total", alternate = {"Total"})
    public int total;

    @SerializedName(value = "pageIndex", alternate = {"PageIndex"})
    public int pageIndex;

    @SerializedName(value = "pageSize", alternate = {"PageSize"})
    public int pageSize;

    @SerializedName(value = "items", alternate = {"Items"})
    public List<T> items;

}
