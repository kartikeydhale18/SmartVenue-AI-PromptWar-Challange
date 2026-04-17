package com.example.smartvenueai.ui.alerts;

public class QueueItem {
    private String name;
    private int waitTimeMinutes;
    private String trend; // "up", "down", "stable"

    public QueueItem() {
        // Required empty constructor for Firestore
    }

    public QueueItem(String name, int waitTimeMinutes, String trend) {
        this.name = name;
        this.waitTimeMinutes = waitTimeMinutes;
        this.trend = trend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWaitTimeMinutes() {
        return waitTimeMinutes;
    }

    public void setWaitTimeMinutes(int waitTimeMinutes) {
        this.waitTimeMinutes = waitTimeMinutes;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }
}
