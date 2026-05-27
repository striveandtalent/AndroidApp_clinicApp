package com.eightbitlab.blurview_sample.Login;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String SP_NAME = "login_sp";
    private static final String KEY_TOKEN = "token";

    // 内存Token（未勾选记住登录时使用）
    private static String memoryToken;

    public static void saveToken(
            Context context,
            String token
    ) {

        memoryToken = token;

        SharedPreferences sp =
                context.getSharedPreferences(
                        SP_NAME,
                        Context.MODE_PRIVATE
                );

        sp.edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    // 仅保存到内存
    public static void saveMemoryToken(String token){

        memoryToken = token;
    }

    public static String getToken(Context context) {

        // 先取内存
        if(memoryToken != null && !memoryToken.isEmpty()){

            return memoryToken;
        }

        SharedPreferences sp =
                context.getSharedPreferences(
                        SP_NAME,
                        Context.MODE_PRIVATE
                );

        return sp.getString(KEY_TOKEN, "");
    }

    public static void clearToken(Context context){

        memoryToken = null;

        SharedPreferences sp =
                context.getSharedPreferences(
                        SP_NAME,
                        Context.MODE_PRIVATE
                );

        sp.edit()
                .remove(KEY_TOKEN)
                .apply();
    }
}
