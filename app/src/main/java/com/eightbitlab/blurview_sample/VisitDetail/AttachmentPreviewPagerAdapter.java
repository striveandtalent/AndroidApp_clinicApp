package com.eightbitlab.blurview_sample.VisitDetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.net.ApiClient;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttachmentPreviewPagerAdapter extends RecyclerView.Adapter<AttachmentPreviewPagerAdapter.VH> {

    private final List<String> imageUrls;
    private final Map<Integer, Float> rotationMap = new HashMap<>();

    public AttachmentPreviewPagerAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : imageUrls;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attachment_preview_image, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String rawUrl = imageUrls.get(position);
        String fullUrl = toAbsoluteUrl(rawUrl);

        Glide.with(holder.photoView.getContext())
                .load(fullUrl)
                .into(holder.photoView);

        float rotation = getRotation(position);
        holder.photoView.setRotation(rotation);
        holder.photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public void rotateAt(int position) {
        float current = getRotation(position);
        float next = current + 90f;
        if (next >= 360f) next = 0f;
        rotationMap.put(position, next);
        notifyItemChanged(position);
    }

    private float getRotation(int position) {
        Float value = rotationMap.get(position);
        return value == null ? 0f : value;
    }

    static class VH extends RecyclerView.ViewHolder {
        PhotoView photoView;

        VH(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
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