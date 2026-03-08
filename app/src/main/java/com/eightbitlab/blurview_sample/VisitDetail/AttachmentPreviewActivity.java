package com.eightbitlab.blurview_sample.VisitDetail;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.net.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttachmentPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_FILE_NAME = "fileName";
    public static final String EXTRA_CONTENT_TYPE = "contentType";

    public static final String EXTRA_URL_LIST = "urlList";
    public static final String EXTRA_FILE_NAME_LIST = "fileNameList";
    public static final String EXTRA_INDEX = "index";

    private TextView tvBack;
    private TextView tvTitle;
    private TextView tvIndex;
    private TextView tvRotate;
    private TextView tvDownload;
    private TextView tvHint;
    private ViewPager2 viewPager;
    private WebView webView;

    private AttachmentPreviewPagerAdapter pagerAdapter;

    private ArrayList<String> urlList = new ArrayList<>();
    private ArrayList<String> fileNameList = new ArrayList<>();
    private int currentIndex = 0;

    private String singleUrl;
    private String singleFileName;
    private String singleContentType;

    private boolean isImageMode = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment_preview);

        bindViews();

        readIntent();

        tvBack.setOnClickListener(v -> finish());

        if (isImageMode) {
            showImagePager();
        } else {
            if (isBlank(singleUrl)) {
                Toast.makeText(this, "附件地址为空", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            showWeb(singleUrl, "当前文件类型不是图片，先尝试网页方式预览。");
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
                    webView.goBack();
                    return;
                }
                finish();
            }
        });
    }

    private void bindViews() {
        tvBack = findViewById(R.id.tvBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvIndex = findViewById(R.id.tvIndex);
        tvRotate = findViewById(R.id.tvRotate);
        tvDownload = findViewById(R.id.tvDownload);
        tvHint = findViewById(R.id.tvHint);
        viewPager = findViewById(R.id.viewPager);
        webView = findViewById(R.id.webView);
    }

    private void readIntent() {
        ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_URL_LIST);
        ArrayList<String> names = getIntent().getStringArrayListExtra(EXTRA_FILE_NAME_LIST);
        int index = getIntent().getIntExtra(EXTRA_INDEX, 0);

        if (urls != null && !urls.isEmpty()) {
            isImageMode = true;
            urlList.clear();
            urlList.addAll(urls);

            fileNameList.clear();
            if (names != null) {
                fileNameList.addAll(names);
            }

            if (index < 0) index = 0;
            if (index >= urlList.size()) index = 0;
            currentIndex = index;
            return;
        }

        singleUrl = getIntent().getStringExtra(EXTRA_URL);
        singleFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        singleContentType = getIntent().getStringExtra(EXTRA_CONTENT_TYPE);

        isImageMode = isSingleImage(singleUrl, singleFileName, singleContentType);

        if (isImageMode) {
            urlList.clear();
            fileNameList.clear();

            urlList.add(singleUrl);
            fileNameList.add(singleFileName);

            currentIndex = 0;
        }
    }

    private boolean isSingleImage(String url, String fileName, String contentType) {
        String ct = safe(contentType).toLowerCase();
        if (ct.startsWith("image/")) {
            return true;
        }

        String lowerUrl = safe(url).toLowerCase();
        String lowerFileName = safe(fileName).toLowerCase();

        return endsWithImageExt(lowerUrl) || endsWithImageExt(lowerFileName);
    }
    private String safe(String s) {
        return s == null ? "" : s;
    }
    private boolean endsWithImageExt(String value) {
        return value.endsWith(".jpg")
                || value.endsWith(".jpeg")
                || value.endsWith(".png")
                || value.endsWith(".webp")
                || value.endsWith(".bmp")
                || value.endsWith(".gif");
    }
    private void showImagePager() {
        viewPager.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);

        tvRotate.setVisibility(View.VISIBLE);
        tvDownload.setVisibility(View.VISIBLE);

        pagerAdapter = new AttachmentPreviewPagerAdapter(urlList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentIndex, false);

        updateHeader(currentIndex);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                updateHeader(position);
            }
        });

        tvRotate.setOnClickListener(v -> {
            if (pagerAdapter != null) {
                pagerAdapter.rotateAt(currentIndex);
            }
        });

        tvDownload.setOnClickListener(v -> saveCurrentImage());
    }

    private void updateHeader(int position) {
        String fileName = "";
        if (position >= 0 && position < fileNameList.size()) {
            fileName = fileNameList.get(position);
        }

        if (isBlank(fileName)) {
            tvTitle.setText("附件预览");
        } else {
            tvTitle.setText(fileName);
        }

        tvIndex.setText((position + 1) + " / " + urlList.size());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showWeb(String url, String hint) {
        viewPager.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        tvRotate.setVisibility(View.GONE);
        tvDownload.setVisibility(View.GONE);

        tvTitle.setText(isBlank(singleFileName) ? "附件预览" : singleFileName);
        tvIndex.setText("");

        if (isBlank(hint)) {
            tvHint.setVisibility(View.GONE);
        } else {
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(hint);
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.loadUrl(url);
    }

    private void saveCurrentImage() {
        if (!isImageMode || currentIndex < 0 || currentIndex >= urlList.size()) {
            Toast.makeText(this, "当前没有可保存的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        final String imageUrl = toAbsoluteUrl(urlList.get(currentIndex));
        String name = "image_" + System.currentTimeMillis() + ".jpg";
        if (currentIndex >= 0 && currentIndex < fileNameList.size() && !isBlank(fileNameList.get(currentIndex))) {
            name = fileNameList.get(currentIndex);
        }
        final String finalName = ensureImageName(name);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Bitmap bitmap = Glide.with(AttachmentPreviewActivity.this)
                        .asBitmap()
                        .load(imageUrl)
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();

                saveBitmapToGallery(bitmap, finalName);

                runOnUiThread(() ->
                        Toast.makeText(AttachmentPreviewActivity.this, "已保存到相册", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(AttachmentPreviewActivity.this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveBitmapToGallery(@NonNull Bitmap bitmap, @NonNull String fileName) throws Exception {
        OutputStream outputStream = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ClinicApp");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("创建相册条目失败");

                outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream == null) throw new Exception("打开输出流失败");

                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                outputStream.flush();
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "ClinicApp");
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new Exception("创建目录失败");
                }

                File file = new File(dir, fileName);
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                outputStream.flush();

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private String ensureImageName(String rawName) {
        String name = isBlank(rawName) ? ("image_" + System.currentTimeMillis() + ".jpg") : rawName.trim();
        String lower = name.toLowerCase();
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png") && !lower.endsWith(".webp")) {
            name = name + ".jpg";
        }
        return name;
    }


    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String toAbsoluteUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return rawUrl;
        }

        String url = rawUrl.trim();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        String baseUrl = ApiClient.getBaseUrl();
        if (baseUrl.endsWith("/") && url.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + url;
        } else if (!baseUrl.endsWith("/") && !url.startsWith("/")) {
            return baseUrl + "/" + url;
        } else {
            return baseUrl + url;
        }
    }
}