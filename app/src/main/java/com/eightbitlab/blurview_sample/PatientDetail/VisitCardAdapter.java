package com.eightbitlab.blurview_sample.PatientDetail;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eightbitlab.blurview_sample.R;
import com.eightbitlab.blurview_sample.TimeFmt;

import java.util.ArrayList;
import java.util.List;

public class VisitCardAdapter extends RecyclerView.Adapter<VisitCardAdapter.VH>{
    public interface OnItemClickListener {
        void onClick(VisitSimpleModel item);
    }

    private final List<VisitSimpleModel> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public VisitCardAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<VisitSimpleModel> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        VisitSimpleModel it = items.get(position);

        String cc = (it.chiefComplaint == null || it.chiefComplaint.trim().isEmpty())
                ? "（无主诉）" : it.chiefComplaint.trim();

        h.tvChiefComplaint.setText("主诉：" + cc);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            h.tvCreateTime.setText(TimeFmt.fmt(it.createTime));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            h.tvUpdateTime.setText(TimeFmt.fmt(it.updateTime));
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(it);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvChiefComplaint, tvCreateTime, tvUpdateTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvChiefComplaint = itemView.findViewById(R.id.tvChiefComplaint);
            tvCreateTime = itemView.findViewById(R.id.tvCreateTime);
            tvUpdateTime = itemView.findViewById(R.id.tvUpdateTime);
        }
    }
}
