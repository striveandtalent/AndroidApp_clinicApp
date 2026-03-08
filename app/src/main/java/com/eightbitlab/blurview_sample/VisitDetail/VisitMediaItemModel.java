package com.eightbitlab.blurview_sample.VisitDetail;

import com.google.gson.annotations.SerializedName;

public class VisitMediaItemModel {
    @SerializedName(value = "id", alternate = {"Id"})
    public long id;

    @SerializedName(value = "mediaId", alternate = {"MediaId"})
    public String mediaId;

    @SerializedName(value = "mediaType", alternate = {"MediaType"})
    public int mediaType;

    @SerializedName(value = "createdAt", alternate = {"CreatedAt"})
    public String createdAt;

    @SerializedName(value = "fileName", alternate = {"FileName"})
    public String fileName;

    @SerializedName(value = "ext", alternate = {"Ext"})
    public String ext;

    @SerializedName(value = "contentType", alternate = {"ContentType"})
    public String contentType;

    @SerializedName(value = "byteSize", alternate = {"ByteSize"})
    public long byteSize;

    @SerializedName(value = "url", alternate = {"Url"})
    public String url;
}
