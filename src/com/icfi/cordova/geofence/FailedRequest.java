package com.icfi.cordova.geofence;

public class FailedRequest {
    private String requestId;
    private CircuitGeofenceEvent circuitGeofenceEvent;
    private String deviceId;

    public FailedRequest(String requestId, CircuitGeofenceEvent circuitGeofenceEvent, String deviceId) {
        this.requestId = requestId;
        this.circuitGeofenceEvent = circuitGeofenceEvent;
        this.deviceId = deviceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public CircuitGeofenceEvent getCircuitGeofenceEvent() {
        return circuitGeofenceEvent;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
