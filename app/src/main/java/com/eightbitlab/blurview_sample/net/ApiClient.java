package com.eightbitlab.blurview_sample.net;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService api;
//    private static final String BASE_URL = "http://10.0.2.2:5219/";//模拟机本地连接
    private static final String BASE_URL = "http://192.168.101.10:6123/";//本地连接
//    private static final String BASE_URL = "http://10.63.237.71:6123/";//IIS连接
    public static String getBaseUrl() {
        return BASE_URL;
    }
    public static ApiService api() {
        if (api != null) return api;

        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(log)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiService.class);
        return api;
    }
}
