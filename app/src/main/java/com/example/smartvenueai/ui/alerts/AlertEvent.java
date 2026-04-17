package com.example.smartvenueai.ui.alerts;

public class AlertEvent {
    private String type; // "CRITICAL", "WARNING", "INFO", "SUCCESS"
    private String message;
    private String timestamp;

    public AlertEvent(String type, String message, String timestamp) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
