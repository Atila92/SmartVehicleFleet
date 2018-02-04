package com.example.atila.smartvehiclefleet;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class OverviewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> vehicleLocationsList;
    private DataProvider dataProvider;
    private Button syncButton;
    private static final String POSTLOCATIONSURL = "http://fleetscanner.store/fleetscanner/insertlocation.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView = (ListView) findViewById(R.id.listView2);
        syncButton = (Button) findViewById(R.id.syncButton);
        dataProvider = new DataProvider(this);
        String[] allVehicleLocations = new String[]{};
        vehicleLocationsList = new ArrayList<String>();
        vehicleLocationsList.addAll(Arrays.asList(allVehicleLocations));
        listAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, vehicleLocationsList);
        listView.setAdapter( listAdapter );
        Cursor cursor = dataProvider.selectAllLocations();
        if (cursor != null){
            while (!cursor.isAfterLast()) {
                listAdapter.add(cursor.getString(cursor.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER)));
                cursor.moveToNext();
            }
            cursor.close();
        }
        listAdapter.notifyDataSetChanged();

        //Listener for listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);
                MapsActivity.vehicleId = selectedItem;
                Intent intent = new Intent(OverviewActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        //sync to remote database listener
        syncButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                postData();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            Intent intent = new Intent(OverviewActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_scan) {
            Intent intent = new Intent(OverviewActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_scan_all) {
            Intent intent = new Intent(OverviewActivity.this, ScanActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            Toast.makeText(getApplicationContext(),"cursor is null",Toast.LENGTH_LONG).show();
        }

        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(locationList);
    }

    public void postData(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        Cursor cursor3 = dataProvider.selectAllLocations();
        if (cursor3 != null){
            params.put("locationsJSON", composeJSONfromSQLite());
            client.post(POSTLOCATIONSURL,params ,new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Toast.makeText(getApplicationContext(),"Sync completed",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    if(statusCode == 404){
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    }else if(statusCode == 500){
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


    }
}
