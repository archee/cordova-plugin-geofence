package com.cowbell.cordova.geofence;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.icfi.cordova.geofence.CircuitGeofenceEvent;
import com.icfi.cordova.geofence.FailedRequestsStorage;
import com.icfi.cordova.geofence.HttpRequest;
import com.icfi.cordova.geofence.HttpRequests;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ReceiveTransitionsIntentService extends IntentService {

    protected GeoNotificationStore store;

    private FailedRequestsStorage failedRequestsStorage;

    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
        store = new GeoNotificationStore(this);
        Logger.setLogger(new Logger(GeofencePlugin.TAG, this, false));
    }

    /**
     * Handles incoming intents
     *
     * @param intent The Intent sent by Location Services. This Intent is provided
     *               to Location Services (inside a PendingIntent) when you call
     *               addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        failedRequestsStorage = new FailedRequestsStorage(this);
        Logger logger = Logger.getLogger();
        logger.log(Log.DEBUG, "ReceiveTransitionsIntentService - onHandleIntent");
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            logger.log(Log.ERROR,
                    "Location Services error: " + Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or Fragment with
             * a broadcast Intent
             */
            /*
             * If there's no error, get the transition type and the IDs of the
             * geofence or geofences that triggered the transition
             */
        } else {
            String deviceId = intent.getStringExtra("com.icfi.cordova.geofence.DEVICEID_EXTRA");
            Log.d(GeofencePlugin.TAG, "The device ID: " + deviceId);

            long now = new Date().getTime();
            // Get the type of transition (entry or exit)
            int transitionType = LocationClient.getGeofenceTransition(intent);
            if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                    || transitionType == Geofence.GEOFENCE_TRANSITION_EXIT
                    || transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
                logger.log(Log.DEBUG, "Geofence transition detected");
                List<Geofence> triggerList = LocationClient
                        .getTriggeringGeofences(intent);

                for (Geofence fence : triggerList) {
                    CircuitGeofenceEvent geofenceEvent = new CircuitGeofenceEvent(fence.getRequestId(), transitionType, now);

                    try {
                        HttpRequests.postGeofenceEvent(geofenceEvent, deviceId);
                    } catch (HttpRequest.HttpRequestException e) {
                        Log.e(GeofencePlugin.TAG, "IOException occurred while trying to POST to endpoint with message: " + e.getMessage());
                        failedRequestsStorage.addFailedRequest(geofenceEvent);
                    }
                }
            }
        }
    }
}
