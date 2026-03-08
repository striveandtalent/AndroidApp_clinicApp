package com.eightbitlab.blurview_sample.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FileUriUtil {

    public static String getDisplayName(Context context, Uri uri) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    result = cursor.getString(index);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static byte[] readBytes(Context context, Uri uri) throws Exception {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            if (in == null) {
                throw new Exception("无法打开文件流");
            }

            out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }
}