package com.eightbitlab.blurview_sample.net;

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

    // 注意：最后一定加 /
//    private static final String BASE_URL = "http://10.0.2.2:5219/";//模拟机本地连接
//    private static final String BASE_URL = "http://192.168.101.10:6123/";//本地连接
//    private static final String BASE_URL = "http://192.168.0.106:6123/";//IIS连接
    private static final String BASE_URL = "http://192.168.0.103:8080/";//IIS连接
//    private static final String BASE_URL = "https://119.84.246.217:45715/";//公网映射
//    private static final String BASE_URL = "https://frp-fee.com:50580/";

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static ApiService api() {
        if (api != null) return api;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiService.class);
        return api;
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