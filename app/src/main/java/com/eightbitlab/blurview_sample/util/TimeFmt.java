package com.eightbitlab.blurview_sample.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TimeFmt {
    private static final DateTimeFormatter IN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter OUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String fmt(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(iso, IN);
            return OUT.format(dt);
        } catch (Exception e) {
            return iso; // 解析失败就原样展示，保证不崩
        }
    }

    // 1970-02-27T00:00:00 -> 1970-02-27
    public static String fmtDateOnly(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";
        int t = s.indexOf('T');
        if (t > 0) return s.substring(0, t);
        // 有些是 "1970-02-27 00:00:00"
        int sp = s.indexOf(' ');
        if (sp > 0) return s.substring(0, sp);
        // 已经是纯日期
        return s;
    }
}
