package com.icfi.cordova.geofence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.util.Log;

import com.cowbell.cordova.geofence.GeofencePlugin;

import java.util.ArrayList;
import java.util.List;

public class FailedRequestsStorage {
    private Context mContext;
    private String deviceId;
    private FailedRequestsDBHelper failedRequestsDBHelper;
    private SQLiteDatabase database;

    public FailedRequestsStorage(Context context) {
        Log.d(GeofencePlugin.TAG, context == null ? "Context in FailedRequestsStorage is null..." : "Context in FailedRequestsStorage is not null!?: " + context);
        this.mContext = context;
        this.failedRequestsDBHelper = FailedRequestsDBHelper.getInstance(mContext);
        deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void addFailedRequest(CircuitGeofenceEvent geofenceEvent) {
        Log.i(GeofencePlugin.TAG, "Inserting a record to the database...");
        SQLiteDatabase writableDatabase = failedRequestsDBHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FailedRequestsDBHelper.LOCATION_COLUMN, geofenceEvent.getLocation());
        contentValues.put(FailedRequestsDBHelper.TYPE_COLUMN, geofenceEvent.getType());
        contentValues.put(FailedRequestsDBHelper.OCCURRED_AT_COLUMN, geofenceEvent.getOccurredAt());

        long result = writableDatabase.insert(FailedRequestsDBHelper.FAILED_REQUESTS_TABLE_NAME, null, contentValues);
        if (result < 0) {
            Log.e(GeofencePlugin.TAG, "Failed to insert a record into the database");
        }
        writableDatabase.close();
    }

    public List<FailedRequest> getAllFailedRequests() {
        Log.d(GeofencePlugin.TAG, "Retrieving all records from the database...");
        ArrayList<FailedRequest> failedRequests = new ArrayList<FailedRequest>();
        database = failedRequestsDBHelper.getReadableDatabase();
        Cursor cursor = database.query(FailedRequestsDBHelper.FAILED_REQUESTS_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            failedRequests.add(
                    new FailedRequest(cursor.getString(0),
                            new CircuitGeofenceEvent(
                                    cursor.getString(1),
                                    cursor.getString(2),
                                    Long.valueOf(cursor.getString(3))
                            ),
                            deviceId)
            );
        }
        cursor.close();
        database.close();
        return failedRequests;
    }

    public void removeFailedRequest(String requestId) {
        Log.i(GeofencePlugin.TAG, "Removing record from the database with requestId: " + requestId);
        database = failedRequestsDBHelper.getWritableDatabase();
        database.delete(FailedRequestsDBHelper.FAILED_REQUESTS_TABLE_NAME,
                FailedRequestsDBHelper.ID + "='" + requestId + "'", null);
        database.close();
    }

    public void clearAll() {
        database = failedRequestsDBHelper.getWritableDatabase();
        database.delete(FailedRequestsDBHelper.FAILED_REQUESTS_TABLE_NAME, null,
                null);
        database.close();
    }
}
