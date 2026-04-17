package com.example.smartvenueai.ui.navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartvenueai.R;

import java.util.List;

public class RouteStepAdapter extends RecyclerView.Adapter<RouteStepAdapter.StepViewHolder> {

    private List<RouteStep> stepList;

    public RouteStepAdapter(List<RouteStep> stepList) {
        this.stepList = stepList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        RouteStep step = stepList.get(position);
        holder.tvInstruction.setText(step.getInstruction());
        
        if (step.isDestination()) {
            holder.tvDistance.setVisibility(View.GONE);
            holder.ivStepIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
            holder.ivStepIcon.setColorFilter(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvDistance.setVisibility(View.VISIBLE);
            holder.tvDistance.setText(step.getDistanceStr());
            holder.ivStepIcon.setImageResource(android.R.drawable.ic_menu_directions);
            holder.ivStepIcon.setColorFilter(android.graphics.Color.parseColor("#1565C0")); // Blue
        }
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstruction;
        TextView tvDistance;
        ImageView ivStepIcon;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ivStepIcon = itemView.findViewById(R.id.ivStepIcon);
        }
    }
}
