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
    public long insertLocation(String vehicleId, String latitude, String longitude, Float accuracy, String timestamp){
        ContentValues values = new ContentValues();
        values.put(DbHelper.REF_VEHICLE_IDENTIFIER, vehicleId);
        values.put(DbHelper.LATITUDE, latitude);
        values.put(DbHelper.LONGITUDE, longitude);
        values.put(DbHelper.ACCURACY, accuracy);
        values.put(DbHelper.TIMESTAMP, timestamp);
        return database.insert(DbHelper.TABLE_LOCATION, null, values);
    }
    //Updates an existing vehicle location
    public long updateLocation(String vehicleId, String latitude, String longitude, Float accuracy, String timestamp){
        ContentValues values = new ContentValues();
        values.put(DbHelper.LATITUDE, latitude);
        values.put(DbHelper.LONGITUDE, longitude);
        values.put(DbHelper.ACCURACY, accuracy);
        values.put(DbHelper.TIMESTAMP, timestamp);
        return database.update(DbHelper.TABLE_LOCATION,values,DbHelper.REF_VEHICLE_IDENTIFIER+"='"+vehicleId+"'",null);
    }
    //selects vehicle location
    public Cursor selectLocation(String vehicleId){
        String[] cols = new String[] {DbHelper.LATITUDE,DbHelper.LONGITUDE,DbHelper.ACCURACY, DbHelper.TIMESTAMP};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATION, cols, DbHelper.REF_VEHICLE_IDENTIFIER +"='"+vehicleId+"'", null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //selects vehicle location
    public Cursor selectAllLocations(){
        String[] cols = new String[] {DbHelper.LOCATION_ID,
                DbHelper.REF_VEHICLE_IDENTIFIER,DbHelper.LATITUDE,DbHelper.LONGITUDE,DbHelper.ACCURACY, DbHelper.TIMESTAMP};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATION, cols, null, null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    //deletes the vehicle from the system
    public void deleteVehicleMapping(String vehicleId)
    {
        database.delete(DbHelper.TABLE_MAPPING, DbHelper.VEHICLE_IDENTIFIER + "='" + vehicleId+"'", null);
        database.delete(DbHelper.TABLE_LOCATION, DbHelper.REF_VEHICLE_IDENTIFIER + "='" + vehicleId+"'", null);
    }
    //resets the vehicle with no location attached
    public void deleteVehicleLocation(String vehicleId)
    {
        database.delete(DbHelper.TABLE_LOCATION, DbHelper.REF_VEHICLE_IDENTIFIER + "='" + vehicleId+"'", null);
    }

    //resets the all vehicle locations
    public void deleteVehicleLocations()
    {
        database.delete(DbHelper.TABLE_LOCATION, null, null);
    }

    //methods for locationlog table
    //Inserts a vehicle location into db
    public long insertLocationLog(String vehicleId, String latitude, String longitude, Float accuracy, String timestamp){
        ContentValues values = new ContentValues();
        values.put(DbHelper.REF_VEHICLE_IDENTIFIER_LOG, vehicleId);
        values.put(DbHelper.LATITUDE_LOG, latitude);
        values.put(DbHelper.LONGITUDE_LOG, longitude);
        values.put(DbHelper.ACCURACY_LOG, accuracy);
        values.put(DbHelper.TIMESTAMP_LOG, timestamp);
        return database.insert(DbHelper.TABLE_LOCATIONLOG, null, values);
    }
    //Updates an existing vehicle location
    public long updateLocationLog(String vehicleId, String latitude, String longitude, Float accuracy, String timestamp){
        ContentValues values = new ContentValues();
        values.put(DbHelper.LATITUDE_LOG, latitude);
        values.put(DbHelper.LONGITUDE_LOG, longitude);
        values.put(DbHelper.ACCURACY_LOG, accuracy);
        values.put(DbHelper.TIMESTAMP_LOG, timestamp);
        return database.update(DbHelper.TABLE_LOCATIONLOG,values,DbHelper.REF_VEHICLE_IDENTIFIER_LOG+"='"+vehicleId+"'",null);
    }
    //selects vehicle location.
    public Cursor selectLocationLog(String vehicleId){
        String[] cols = new String[] {DbHelper.LATITUDE_LOG,DbHelper.LONGITUDE_LOG,DbHelper.ACCURACY_LOG, DbHelper.TIMESTAMP_LOG};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATIONLOG, cols, DbHelper.REF_VEHICLE_IDENTIFIER_LOG +"='"+vehicleId+"'", null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //select 3 latest vehicle locations
    public Cursor selectLatestLocationLog(String vehicleId){
        String[] cols = new String[] {DbHelper.LOCATION_ID_LOG,
                DbHelper.REF_VEHICLE_IDENTIFIER_LOG,DbHelper.LATITUDE_LOG,DbHelper.LONGITUDE_LOG,DbHelper.ACCURACY_LOG, DbHelper.TIMESTAMP_LOG};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATIONLOG, cols, DbHelper.REF_VEHICLE_IDENTIFIER_LOG +"='"+vehicleId+"'", null, null, null, DbHelper.LOCATION_ID_LOG +" DESC", "3");
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //selects vehicle location
    public Cursor selectAllLocationsLogs(){
        String[] cols = new String[] {DbHelper.LOCATION_ID_LOG,
                DbHelper.REF_VEHICLE_IDENTIFIER_LOG,DbHelper.LATITUDE_LOG,DbHelper.LONGITUDE_LOG,DbHelper.ACCURACY_LOG, DbHelper.TIMESTAMP_LOG};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATIONLOG, cols, null, null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //selects vehicle location
    public Cursor selectAllLocationsLogsGrouped(){
        String[] cols = new String[] {DbHelper.REF_VEHICLE_IDENTIFIER_LOG};
        Cursor cursor = database.query(true, DbHelper.TABLE_LOCATIONLOG, cols, null, null, DbHelper.REF_VEHICLE_IDENTIFIER_LOG, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    //deletes the vehicle from the system
    public void deleteVehicleMappingLog(String vehicleId)
    {
        database.delete(DbHelper.TABLE_MAPPING, DbHelper.VEHICLE_IDENTIFIER + "='" + vehicleId+"'", null);
        database.delete(DbHelper.TABLE_LOCATIONLOG, DbHelper.REF_VEHICLE_IDENTIFIER_LOG + "='" + vehicleId+"'", null);
    }
    //resets the vehicle with no location attached
    public void deleteVehicleLocationLog(String vehicleId)
    {
        database.delete(DbHelper.TABLE_LOCATIONLOG, DbHelper.REF_VEHICLE_IDENTIFIER_LOG + "='" + vehicleId+"'", null);
    }

    //resets the all vehicle locations
    public void deleteVehicleLocationsLogs()
    {
        database.delete(DbHelper.TABLE_LOCATIONLOG, null, null);
    }


}
