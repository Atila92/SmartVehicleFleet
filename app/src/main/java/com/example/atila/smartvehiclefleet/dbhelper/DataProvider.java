package com.example.atila.smartvehiclefleet.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Atila on 10-Oct-17.
 */

public class DataProvider {

    private DbHelper dbHelper;
    private SQLiteDatabase database;


    public DataProvider(Context context){
        dbHelper = new DbHelper(context);
        database = dbHelper.getWritableDatabase();
    }
    //Inserts a beacon-vehicle mapping into db
    public long insertMapping(String beaconId, String vehicleId){
        ContentValues values = new ContentValues();
        values.put(DbHelper.BEACON_IDENTIFIER, beaconId);
        values.put(DbHelper.VEHICLE_IDENTIFIER, vehicleId);
        return database.insert(DbHelper.TABLE_MAPPING, null, values);
    }
    //Updates an existing mappings
    public long updateMapping(String beaconId, String vehicleId){
        ContentValues values = new ContentValues();
        values.put(DbHelper.VEHICLE_IDENTIFIER, vehicleId);
        return database.update(DbHelper.TABLE_MAPPING,values,DbHelper.BEACON_IDENTIFIER+"='"+beaconId+"'",null);
    }
    //Select all mappings from sqlite db
    public Cursor selectAllMappings(){
        String[] cols = new String[] {DbHelper.VEHICLE_IDENTIFIER};
        Cursor cursor = database.query(true, DbHelper.TABLE_MAPPING, cols, null, null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    //Select a specific vehicle identifier from sqlite db
    public Cursor selectVehicleIdentifier(String beaconId){
        String[] cols = new String[] {DbHelper.VEHICLE_IDENTIFIER};
        Cursor cursor = database.query(true, DbHelper.TABLE_MAPPING, cols, DbHelper.BEACON_IDENTIFIER +"='"+beaconId+"'", null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //Select a specific beacon identifier from sqlite db
    public Cursor selectBeaconIdentifier(String vehicleId){
        String[] cols = new String[] {DbHelper.BEACON_IDENTIFIER};
        Cursor cursor = database.query(true, DbHelper.TABLE_MAPPING, cols, DbHelper.VEHICLE_IDENTIFIER +"='"+vehicleId+"'", null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //Inserts a vehicle location into db
    public long insertLocation(String vehicleId, String latitude, String longitude, Float accuracy){
        ContentValues values = new ContentValues();
        values.put(DbHelper.REF_VEHICLE_IDENTIFIER, vehicleId);
        values.put(DbHelper.LATITUDE, latitude);
        values.put(DbHelper.LONGITUDE, longitude);
        values.put(DbHelper.ACCURACY, accuracy);
        return database.insert(DbHelper.TABLE_LOCATION, null, values);
    }
    //Updates an existing vehicle location
    public long updateLocation(String vehicleId, String latitude, String longitude, Float accuracy){
        ContentValues values = new ContentValues();
        values.put(DbHelper.LATITUDE, latitude);
        values.put(DbHelper.LONGITUDE, longitude);
        values.put(DbHelper.ACCURACY, accuracy);
        return database.update(DbHelper.TABLE_LOCATION,values,DbHelper.REF_VEHICLE_IDENTIFIER+"='"+vehicleId+"'",null);
    }
    //selects vehicle location
    public Cursor selectLocation(String vehicleId){
        String[] cols = new String[] {DbHelper.LATITUDE,DbHelper.LONGITUDE,DbHelper.ACCURACY};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATION, cols, DbHelper.REF_VEHICLE_IDENTIFIER +"='"+vehicleId+"'", null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //selects vehicle location
    public Cursor selectAllLocations(){
        String[] cols = new String[] {DbHelper.LOCATION_ID,
                DbHelper.REF_VEHICLE_IDENTIFIER,DbHelper.LATITUDE,DbHelper.LONGITUDE,DbHelper.ACCURACY};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATION, cols, null, null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

}
