package com.eightbitlab.blurview_sample.Patient_Search;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.PatientDetail.PatientModel;

import java.util.ArrayList;
import java.util.List;
public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.VH> {

    public interface OnItemClick {
        void onClick(PatientModel item);
    }

    private final List<PatientModel> data = new ArrayList<>();
    private final OnItemClick onItemClick;

    public PatientAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void submit(List<PatientModel> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 先用项目自带的 list_item.xml（如果不合适你再换）
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PatientModel item = data.get(position);

        holder.tvTitle.setText((item.name == null ? "" : item.name)
                + (item.phone == null ? "" : ("  " + item.phone)));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
