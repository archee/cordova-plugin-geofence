package com.icfi.cordova.geofence;

import com.cowbell.cordova.geofence.Gson;
import com.google.android.gms.location.Geofence;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class CircuitGeofenceEvent implements Serializable {
    @Expose private String location;
    @Expose private String type;
    @Expose private long occurredAt;

    public CircuitGeofenceEvent() {}

    public CircuitGeofenceEvent(String location, int transitionType, long occurredAt) {
        this.location = location;
        this.occurredAt = occurredAt;

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                this.type = "enter";
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                this.type = "exit";
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                this.type = "dwelling";
                break;
        }
    }

    public CircuitGeofenceEvent(String location, String transitionType, long occurredAt) {
        this.location = location;
        this.type = transitionType;
        this.occurredAt = occurredAt;
    }

    public String toJson() {
        return Gson.get().toJson(this);
    }

    @Override
    public String toString() {
        return "[location: " + location + " \n" +
                "type: " + type + " \n" +
                "occurredAt: " + String.valueOf(occurredAt) + "]";
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public long getOccurredAt() {
        return occurredAt;
    }
}
