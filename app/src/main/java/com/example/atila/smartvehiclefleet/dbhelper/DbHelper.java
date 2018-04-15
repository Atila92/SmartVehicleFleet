package com.example.atila.smartvehiclefleet.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Time;

/**
 * Created by Atila on 10-Oct-17.
 */

public class DbHelper extends SQLiteOpenHelper {

    //Log tag
    private static final String LOG = "DbHelper alive";
    //DB name
    private static final String DB_NAME = "smartvehiclefleet_db";
    //DB version
    private static final int DB_VERSION = 5;
    //Tables names
    public static final String TABLE_MAPPING = "tblbeaconvehiclemapping";
    public static final String TABLE_LOCATION = "tbllocation";
    public static final String TABLE_LOCATIONLOG = "tbllocationlog";

    //tblmapping column names
    public static final String MAPPING_ID = "mappingid";
    public static final String BEACON_IDENTIFIER = "beaconidentifier";
    public static final String VEHICLE_IDENTIFIER = "vehicleidentifier";

    //tbllocation column names
    public static final String LOCATION_ID = "locationid";
    public static final String REF_VEHICLE_IDENTIFIER = "refvehicleidentifier";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ACCURACY = "accuracy";
    public static final String TIMESTAMP = "timestamp";

    //tbllocationlog column names
    public static final String LOCATION_ID_LOG = "locationid";
    public static final String REF_VEHICLE_IDENTIFIER_LOG = "refvehicleidentifier";
    public static final String LATITUDE_LOG = "latitude";
    public static final String LONGITUDE_LOG = "longitude";
    public static final String ACCURACY_LOG = "accuracy";
    public static final String TIMESTAMP_LOG = "timestamp";

    //create tblbeaconvehiclemapping statement
    private static final String CREATE_TABLE_BEACONVEHICLEMAPPING = "CREATE TABLE "
            + TABLE_MAPPING + "(" + MAPPING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + BEACON_IDENTIFIER + " TEXT,"
            + VEHICLE_IDENTIFIER + " TEXT)";

    //create tbllocation statement
    private static final String CREATE_TABLE_LOCATION = "CREATE TABLE "
            + TABLE_LOCATION + "(" + LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + REF_VEHICLE_IDENTIFIER + " TEXT,"
            + LATITUDE + " TEXT," + LONGITUDE + " TEXT," + ACCURACY + " REAL,"+ TIMESTAMP+" TEXT)";

    //create tbllocationlog statement
    private static final String CREATE_TABLE_LOCATIONLOG = "CREATE TABLE "
            + TABLE_LOCATIONLOG + "(" + LOCATION_ID_LOG + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + REF_VEHICLE_IDENTIFIER_LOG + " TEXT,"
            + LATITUDE_LOG + " TEXT," + LONGITUDE_LOG + " TEXT," + ACCURACY_LOG + " REAL,"+ TIMESTAMP_LOG+" TEXT)";

    public DbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create tables
        db.execSQL(CREATE_TABLE_BEACONVEHICLEMAPPING);
        db.execSQL(CREATE_TABLE_LOCATION);
        db.execSQL(CREATE_TABLE_LOCATIONLOG);
        Log.d("tables created","tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPPING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONLOG);
        //create the new tables
        onCreate(db);
    }
}
