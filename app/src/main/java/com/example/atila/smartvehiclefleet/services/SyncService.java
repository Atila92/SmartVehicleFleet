package com.example.atila.smartvehiclefleet.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Atila on 04-Feb-18.
 */

public class SyncService {

    private static final String POSTLOCATIONSURL = "http://fleetscanner.store/fleetscanner/insertlocation.php";
    private static final String POSTLOCATIONSLOGURL = "http://fleetscanner.store/fleetscanner/insertlocationlog.php";
    private static final String DELETELOCATIONSURL = "http://fleetscanner.store/fleetscanner/deletelocation.php";
    private static final String DELETELOCATIONSLOGURL = "http://fleetscanner.store/fleetscanner/deletelocationlog.php";
    private static final String DELETEALLLOCATIONSURL = "http://fleetscanner.store/fleetscanner/deletealllocations.php";
    private static final String DELETEALLLOCATIONSLOGURL = "http://fleetscanner.store/fleetscanner/deletealllocationslog.php";
    private DataProvider dataProvider;
    private Context context;
    private Context activityContext;


    public SyncService(Context context, Context activityContext){
        this.context = context;
        this.activityContext = activityContext;
    }
    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> locationList;
        locationList = new ArrayList<HashMap<String, String>>();
        Cursor cursor2 = dataProvider.selectAllLocations();
        if (cursor2.getCount() >0){
            while (!cursor2.isAfterLast()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Locationid", cursor2.getString(cursor2.getColumnIndex(DbHelper.LOCATION_ID)));
                map.put("Refvehicleidentifier", cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER)));
                map.put("Latitude", cursor2.getString(cursor2.getColumnIndex(DbHelper.LATITUDE)));
                map.put("Longitude", cursor2.getString(cursor2.getColumnIndex(DbHelper.LONGITUDE)));
                map.put("Accuracy", cursor2.getString(cursor2.getColumnIndex(DbHelper.ACCURACY)));
                map.put("Timestamp", cursor2.getString(cursor2.getColumnIndex(DbHelper.TIMESTAMP)));
                locationList.add(map);
                cursor2.moveToNext();
            }
            cursor2.close();
        }else{
            Toast.makeText(context,"cursor is null",Toast.LENGTH_LONG).show();
        }

        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(locationList);
    }

    public void postData(){
        final ProgressDialog progress = new ProgressDialog(activityContext);
        progress.setTitle("Synchronizing");
        progress.setMessage("Wait while synchronizing to remote db..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        dataProvider = new DataProvider(context);
        Cursor cursor3 = dataProvider.selectAllLocations();
        if (cursor3.getCount() >0){
            params.put("locationsJSON", composeJSONfromSQLite());
            client.post(POSTLOCATIONSURL,params ,new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    progress.dismiss();
                    Toast.makeText(context,"Sync completed",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    progress.dismiss();
                    if(statusCode == 404){
                        Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                    }else if(statusCode == 500){
                        Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                    }
                }
            });
            cursor3.close();
        }


    }

    //for deleting from remote db
    public String composeDeleteJSONfromSQLite(String vehicleId){
        ArrayList<HashMap<String, String>> deleteList;
        deleteList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Refvehicleidentifier", vehicleId);
        deleteList.add(map);

        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(deleteList);
    }

    public void deleteData(final String vehicleId){
        final ProgressDialog progress = new ProgressDialog(activityContext);
        progress.setTitle("Deleting location");
        progress.setMessage("Wait while resetting vehicle location data..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("deleteJSON", composeDeleteJSONfromSQLite(vehicleId));
        client.post(DELETELOCATIONSURL,params ,new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                progress.dismiss();
                Toast.makeText(context,"Vehicle "+vehicleId+" deleted!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progress.dismiss();
                if(statusCode == 404){
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    //delete all vehicle locations
    public void deleteAllLocations(){
        final ProgressDialog progress = new ProgressDialog(activityContext);
        progress.setTitle("Deleting locations");
        progress.setMessage("Wait while resetting vehicle location data..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(DELETEALLLOCATIONSURL,new AsyncHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                progress.dismiss();
                Toast.makeText(context,"Vehicle locations deleted",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show();

            }
        });
    }

    //For locationlog experiments
    public String composeJSONfromSQLiteLog(){
        ArrayList<HashMap<String, String>> locationList;
        locationList = new ArrayList<HashMap<String, String>>();
        Cursor cursor2 = dataProvider.selectAllLocationsLogs();
        if (cursor2.getCount() >0){
            while (!cursor2.isAfterLast()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Locationid", cursor2.getString(cursor2.getColumnIndex(DbHelper.LOCATION_ID_LOG)));
                map.put("Refvehicleidentifier", cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER_LOG)));
                map.put("Latitude", cursor2.getString(cursor2.getColumnIndex(DbHelper.LATITUDE_LOG)));
                map.put("Longitude", cursor2.getString(cursor2.getColumnIndex(DbHelper.LONGITUDE_LOG)));
                map.put("Accuracy", cursor2.getString(cursor2.getColumnIndex(DbHelper.ACCURACY_LOG)));
                map.put("Timestamp", cursor2.getString(cursor2.getColumnIndex(DbHelper.TIMESTAMP_LOG)));
                locationList.add(map);
                cursor2.moveToNext();
            }
            cursor2.close();
        }else{
            Toast.makeText(context,"cursor is null",Toast.LENGTH_LONG).show();
        }

        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(locationList);
    }

    public void postDataLog(){
        final ProgressDialog progress = new ProgressDialog(activityContext);
        progress.setTitle("Synchronizing log");
        progress.setMessage("Wait while synchronizing to remote db..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        dataProvider = new DataProvider(context);
        Cursor cursor3 = dataProvider.selectAllLocationsLogs();
        if (cursor3.getCount() >0){
            params.put("locationslogJSON", composeJSONfromSQLiteLog());
            client.post(POSTLOCATIONSLOGURL,params ,new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    progress.dismiss();
                    Toast.makeText(context,"Sync completed",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    progress.dismiss();
                    if(statusCode == 404){
                        Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                    }else if(statusCode == 500){
                        Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                    }
                }
            });
            cursor3.close();
        }


    }

    public void deleteDataLog(final String vehicleId){
        final ProgressDialog progress = new ProgressDialog(activityContext);
        progress.setTitle("Deleting location");
        progress.setMessage("Wait while resetting vehicle location data..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("deleteJSON", composeDeleteJSONfromSQLite(vehicleId));
        client.post(DELETELOCATIONSLOGURL,params ,new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                progress.dismiss();
                Toast.makeText(context,"Vehicle "+vehicleId+" deleted!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progress.dismiss();
                if(statusCode == 404){
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    //delete all vehicle locations
    public void deleteAllLocationsLog(){
        final ProgressDialog progress = new ProgressDialog(activityContext);
        progress.setTitle("Deleting locations");
        progress.setMessage("Wait while resetting vehicle location data..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(DELETEALLLOCATIONSLOGURL,new AsyncHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                progress.dismiss();
                Toast.makeText(context,"Vehicle locations deleted",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show();

            }
        });
    }


}
