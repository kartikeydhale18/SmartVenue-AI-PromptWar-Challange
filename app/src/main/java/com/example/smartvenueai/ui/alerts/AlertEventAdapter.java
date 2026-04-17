package com.example.smartvenueai.ui.alerts;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartvenueai.R;

import java.util.List;

public class AlertEventAdapter extends RecyclerView.Adapter<AlertEventAdapter.ViewHolder> {

    private List<AlertEvent> alertList;

    public AlertEventAdapter(List<AlertEvent> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertEvent event = alertList.get(position);
        holder.tvAlertMessage.setText(event.getMessage());
        holder.tvAlertTimestamp.setText(event.getTimestamp());

        // Apply dynamic styling based on the event type
        switch (event.getType()) {
            case "CRITICAL":
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#B71C1C")); // alert_critical
                holder.tvAlertMessage.setTextColor(Color.WHITE);
                holder.tvAlertTimestamp.setTextColor(Color.WHITE);
                holder.ivAlertIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.ivAlertIcon.setColorFilter(Color.WHITE);
                break;
            case "WARNING":
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FFE0B2")); // alert_yellow_bg
                holder.tvAlertMessage.setTextColor(Color.parseColor("#F57F17")); // alert_yellow
                holder.tvAlertTimestamp.setTextColor(Color.parseColor("#616161")); // text_secondary
                holder.ivAlertIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.ivAlertIcon.setColorFilter(Color.parseColor("#F57F17"));
                break;
            case "SUCCESS":
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // alert_green_bg
                holder.tvAlertMessage.setTextColor(Color.parseColor("#1B5E20")); // alert_green
                holder.tvAlertTimestamp.setTextColor(Color.parseColor("#616161"));
                holder.ivAlertIcon.setImageResource(android.R.drawable.checkbox_on_background);
                holder.ivAlertIcon.setColorFilter(Color.parseColor("#1B5E20"));
                break;
            case "INFO":
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#BBDEFB")); // alert_blue_bg
                holder.tvAlertMessage.setTextColor(Color.parseColor("#0D47A1")); // alert_blue
                holder.tvAlertTimestamp.setTextColor(Color.parseColor("#616161"));
                holder.ivAlertIcon.setImageResource(android.R.drawable.ic_dialog_info);
                holder.ivAlertIcon.setColorFilter(Color.parseColor("#0D47A1"));
                break;
            default: // Normal
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#F5F5F5")); // surface
                holder.tvAlertMessage.setTextColor(Color.parseColor("#212121")); // text_primary
                holder.tvAlertTimestamp.setTextColor(Color.parseColor("#616161"));
                holder.ivAlertIcon.setImageResource(android.R.drawable.ic_popup_reminder);
                holder.ivAlertIcon.setColorFilter(Color.parseColor("#616161"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardContainer;
        ImageView ivAlertIcon;
        TextView tvAlertMessage;
        TextView tvAlertTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            ivAlertIcon = itemView.findViewById(R.id.ivAlertIcon);
            tvAlertMessage = itemView.findViewById(R.id.tvAlertMessage);
            tvAlertTimestamp = itemView.findViewById(R.id.tvAlertTimestamp);
        }
    }
}
