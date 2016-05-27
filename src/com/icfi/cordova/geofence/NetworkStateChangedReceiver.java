package com.icfi.cordova.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cowbell.cordova.geofence.GeofencePlugin;

public class NetworkStateChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(GeofencePlugin.TAG, "onReceive() called for " + NetworkStateChangedReceiver.class.getSimpleName());
        if (isOnline(context)) {
            Log.d(GeofencePlugin.TAG, "Device is online. Checking for failed requests in DB.");
            FailedRequestsStorage failedRequestsStorage = new FailedRequestsStorage(context);

            if (failedRequestsStorage.getAllFailedRequests().size() > 0) {
                Log.d(GeofencePlugin.TAG, "Failed requests found. Launching " + RetryFailedRequestsService.class.getSimpleName());
                Intent retryService = new Intent(context, RetryFailedRequestsService.class);
                context.startService(retryService);
            } else {
                Log.d(GeofencePlugin.TAG, "No failed requests found. Doing nothing.");
            }
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;

        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED);
    }
}
