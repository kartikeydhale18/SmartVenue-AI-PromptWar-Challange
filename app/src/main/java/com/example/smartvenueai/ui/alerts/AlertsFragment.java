package com.example.smartvenueai.ui.alerts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartvenueai.MainActivity;
import com.example.smartvenueai.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private QueueAdapter adapter;
    private List<QueueItem> queueList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hamburger menu
        view.findViewById(R.id.btnOpenDrawerAlerts).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        // Tapping the orange alert banner opens Alerts & Live Feed
        view.findViewById(R.id.alertBanner).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AlertsLiveActivity.class))
        );

        // Initialize RecyclerView
        RecyclerView rvQueues = view.findViewById(R.id.rvQueues);
        rvQueues.setLayoutManager(new LinearLayoutManager(getContext()));
        queueList = new ArrayList<>();
        adapter = new QueueAdapter();
        rvQueues.setAdapter(adapter);

        // Fetch real-time data from Firestore
        FirebaseFirestore.getInstance().collection("queues")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), "Error loading alerts: " + error.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    if (value != null) {
                        queueList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                QueueItem item = doc.toObject(QueueItem.class);
                                queueList.add(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.setQueueList(queueList);
                    }
                });
    }
}
