package com.eightbitlab.blurview_sample.net;

import android.content.Context;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static ApiService api;
    private static Retrofit retrofit;
    private static String currentBaseUrl;

    /**
     * 获取当前 BaseUrl（给图片预览、视频预览拼接地址用）
     */
    public static String getBaseUrl(Context context) {
        return AppSettings.getBaseUrl(context);
    }

    /**
     * 兼容你原来部分调用方式：
     * 但前提是先 init 过一次
     */
    public static String getBaseUrl() {
        return currentBaseUrl == null ? "" : currentBaseUrl;
    }

    /**
     * 应用启动时可调用一次，也可以在首次请求时自动初始化
     */
    public static void init(Context context) {
        String baseUrl = AppSettings.getBaseUrl(context);
        if (api == null || retrofit == null || !baseUrl.equals(currentBaseUrl)) {
            buildRetrofit(baseUrl);
        }
    }

    /**
     * 正常网络请求统一走这个
     */
    public static ApiService api(Context context) {
        init(context);
        return api;
    }

    /**
     * 为了尽量少改你现有代码，保留旧方法
     * 但建议后面逐步改成 api(context)
     */
    public static ApiService api() {
        if (api == null) {
            throw new IllegalStateException("ApiClient 尚未初始化，请先调用 ApiClient.init(context) 或 ApiClient.api(context)");
        }
        return api;
    }

    /**
     * 当设置页改了服务器地址后，调用这个强制重建
     */
    public static void reset(Context context) {
        buildRetrofit(AppSettings.getBaseUrl(context));
    }

    private static void buildRetrofit(String baseUrl) {
        currentBaseUrl = baseUrl;

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiService.class);
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            return new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}