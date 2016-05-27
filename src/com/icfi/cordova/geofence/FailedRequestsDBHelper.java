package com.icfi.cordova.geofence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FailedRequestsDBHelper extends SQLiteOpenHelper {

    private static FailedRequestsDBHelper mInstance;

    /**
     * the name of the table
     */
    public static final String FAILED_REQUESTS_TABLE_NAME = "failedrequests";

    /**
     * the id column of the table LOCALSTORAGE_TABLE_NAME
     */
    public static final String ID = "_id";

    public static final String LOCATION_COLUMN = "location";
    public static final String TYPE_COLUMN = "type";
    public static final String OCCURRED_AT_COLUMN = "occurredAt";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "failedrequests.db";
    private static final String FAILED_REQUESTS_TABLE_CREATE = "CREATE TABLE "
            + FAILED_REQUESTS_TABLE_NAME + " (" + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            LOCATION_COLUMN + " TEXT NOT NULL," +
            TYPE_COLUMN + " TEXT NOT NULL," +
            OCCURRED_AT_COLUMN + " TEXT NOT NULL);";

    public FailedRequestsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static FailedRequestsDBHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new FailedRequestsDBHelper(ctx);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FAILED_REQUESTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(FailedRequestsDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + FAILED_REQUESTS_TABLE_NAME);
        onCreate(db);
    }
}
