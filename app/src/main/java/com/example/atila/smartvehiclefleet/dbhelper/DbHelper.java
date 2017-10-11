package com.example.atila.smartvehiclefleet.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Atila on 10-Oct-17.
 */

public class DbHelper extends SQLiteOpenHelper {

    //Log tag
    private static final String LOG = "DbHelper alive";
    //DB name
    private static final String DB_NAME = "smartvehiclefleet_db";
    //DB version
    private static final int DB_VERSION = 1;
    //Tables names
    public static final String TABLE_MAPPING = "tblbeaconvehiclemapping";

    //tbltask column names
    public static final String MAPPING_ID = "mappingid";
    public static final String BEACON_IDENTIFIER = "beaconidentifier";
    public static final String VEHICLE_IDENTIFIER = "vehicleidentifier";

    //create tblbeaconvehiclemapping statement
    private static final String CREATE_TABLE_BEACONVEHICLEMAPPING = "CREATE TABLE "
            + TABLE_MAPPING + "(" + MAPPING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + BEACON_IDENTIFIER + " TEXT,"
            + VEHICLE_IDENTIFIER + " TEXT)";


    public DbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create tables
        db.execSQL(CREATE_TABLE_BEACONVEHICLEMAPPING);
        Log.d("tables created","tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPPING);
        //create the new tables
        onCreate(db);
    }
}
