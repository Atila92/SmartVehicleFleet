package com.example.atila.smartvehiclefleet;

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
        if (cursor2 != null){
            while (!cursor2.isAfterLast()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Locationid", cursor2.getString(cursor2.getColumnIndex(DbHelper.LOCATION_ID)));
                map.put("Refvehicleidentifier", cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER)));
                map.put("Latitude", cursor2.getString(cursor2.getColumnIndex(DbHelper.LATITUDE)));
                map.put("Longitude", cursor2.getString(cursor2.getColumnIndex(DbHelper.LONGITUDE)));
                map.put("Accuracy", cursor2.getString(cursor2.getColumnIndex(DbHelper.ACCURACY)));
                //map.put("Timestamp", cursor.getString(1));
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
        if (cursor3 != null){
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
        }


    }
}
