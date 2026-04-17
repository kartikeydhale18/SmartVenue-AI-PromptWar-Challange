package com.example.smartvenueai.ui.alerts;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartvenueai.R;

import java.util.ArrayList;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private List<QueueItem> queueList = new ArrayList<>();

    public void setQueueList(List<QueueItem> queueList) {
        this.queueList = queueList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_queue_row, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        QueueItem item = queueList.get(position);
        holder.tvQueueName.setText(item.getName() != null ? item.getName() : "Unknown");
        holder.tvWaitTime.setText(String.valueOf(item.getWaitTimeMinutes()));

        String trend = item.getTrend() != null ? item.getTrend().toLowerCase() : "stable";
        
        switch (trend) {
            case "down":
                holder.ivTrendArrow.setImageResource(android.R.drawable.arrow_down_float);
                // using a green-ish color for trend_down
                ImageViewCompat.setImageTintList(holder.ivTrendArrow, ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                break;
            case "stable":
            case "up":
            default:
                holder.ivTrendArrow.setImageResource(android.R.drawable.arrow_up_float);
                // using an orange-ish/red color for trend_up
                ImageViewCompat.setImageTintList(holder.ivTrendArrow, ColorStateList.valueOf(Color.parseColor("#E65100")));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return queueList.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView tvQueueName;
        TextView tvWaitTime;
        ImageView ivTrendArrow;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQueueName = itemView.findViewById(R.id.tvQueueName);
            tvWaitTime = itemView.findViewById(R.id.tvWaitTime);
            ivTrendArrow = itemView.findViewById(R.id.ivTrendArrow);
        }
    }
}
