package com.example.smartvenueai.ui.navigation;

public class RouteStep {
    private String instruction;
    private String distanceStr;
    private boolean isDestination;

    public RouteStep(String instruction, String distanceStr, boolean isDestination) {
        this.instruction = instruction;
        this.distanceStr = distanceStr;
        this.isDestination = isDestination;
    }

    public String getInstruction() { return instruction; }
    public String getDistanceStr() { return distanceStr; }
    public boolean isDestination() { return isDestination; }
}
