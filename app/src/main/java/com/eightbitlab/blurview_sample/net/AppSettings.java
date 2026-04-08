package com.eightbitlab.blurview_sample.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class AppSettings {

    private static final String SP_NAME = "clinic_app_settings";

    // 当前选中的环境
    private static final String KEY_CURRENT_ENV = "current_env";

    // 2个环境各自的地址
    private static final String KEY_BASE_URL_LOCAL = "base_url_local";
    private static final String KEY_BASE_URL_PUBLIC = "base_url_public";

    public static final String ENV_LOCAL = "local";
    public static final String ENV_PUBLIC = "public";

    private static final String DEFAULT_BASE_URL_LOCAL = "http://192.168.0.106:8080/";
    private static final String DEFAULT_BASE_URL_PUBLIC = "https://frp-fee.com:50580/";

    private static SharedPreferences sp(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static String getCurrentEnv(Context context) {
        return sp(context).getString(KEY_CURRENT_ENV, ENV_PUBLIC);
    }

    public static void setCurrentEnv(Context context, String env) {
        sp(context).edit().putString(KEY_CURRENT_ENV, env).apply();
    }

    public static String getCurrentEnvDisplayName(Context context) {
        String env = getCurrentEnv(context);
        switch (env) {
            case ENV_LOCAL:
                return "本地";
            case ENV_PUBLIC:
            default:
                return "线上";
        }
    }

    public static String getBaseUrl(Context context) {
        String env = getCurrentEnv(context);
        return getBaseUrlByEnv(context, env);
    }

    public static String getBaseUrlByEnv(Context context, String env) {
        SharedPreferences prefs = sp(context);

        String url;
        switch (env) {
            case ENV_LOCAL:
                url = prefs.getString(KEY_BASE_URL_LOCAL, DEFAULT_BASE_URL_LOCAL);
                break;
            case ENV_PUBLIC:
            default:
                url = prefs.getString(KEY_BASE_URL_PUBLIC, DEFAULT_BASE_URL_PUBLIC);
                break;
        }

        return normalizeBaseUrl(url);
    }

    public static void setBaseUrl(Context context, String baseUrl) {
        String env = getCurrentEnv(context);
        setBaseUrlByEnv(context, env, baseUrl);
    }

    public static void setBaseUrlByEnv(Context context, String env, String baseUrl) {
        String normalized = normalizeBaseUrl(baseUrl);
        SharedPreferences.Editor editor = sp(context).edit();

        switch (env) {
            case ENV_LOCAL:
                editor.putString(KEY_BASE_URL_LOCAL, normalized);
                break;
            case ENV_PUBLIC:
            default:
                editor.putString(KEY_BASE_URL_PUBLIC, normalized);
                break;
        }
        editor.apply();
    }

    public static String normalizeBaseUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return DEFAULT_BASE_URL_PUBLIC;
        }

        url = url.trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }

        return url;
    }

    public static boolean isValidBaseUrl(String url) {
        if (TextUtils.isEmpty(url)) return false;

        String normalized = normalizeBaseUrl(url);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }
}