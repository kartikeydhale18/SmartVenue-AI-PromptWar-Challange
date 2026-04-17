package com.example.smartvenueai.ui.alerts;

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartvenueai.R;

import java.util.ArrayList;
import java.util.List;

public class AlertsLiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts_live);

        // Back button
        findViewById(R.id.btnBackFromLive).setOnClickListener(v -> finish());

        // Set up Live Alerts RecyclerView
        RecyclerView rvLiveAlerts = findViewById(R.id.rvLiveAlerts);
        rvLiveAlerts.setLayoutManager(new LinearLayoutManager(this));

        List<AlertEvent> events = new ArrayList<>();
        events.add(new AlertEvent("CRITICAL", "Stampede Risk Detected! Section 215 Stairwell A1.", "Just now"));
        events.add(new AlertEvent("WARNING", "Congestion Warning - Gate A Traffic Increasing", "2 mins ago"));
        events.add(new AlertEvent("SUCCESS", "Navigation Rerouted - Crowd identified on Route 1", "5 mins ago"));
        events.add(new AlertEvent("INFO", "Kickoff in 10 minutes", "10 mins ago"));
        events.add(new AlertEvent("DEFAULT", "Security Patrol shift started", "15 mins ago"));

        AlertEventAdapter adapter = new AlertEventAdapter(events);
        rvLiveAlerts.setAdapter(adapter);
    }
}
