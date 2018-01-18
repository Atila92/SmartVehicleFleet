package com.example.atila.smartvehiclefleet;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.RegionUtils;
import com.estimote.coresdk.observation.utils.Proximity;
import com.estimote.coresdk.recognition.packets.EstimoteLocation;
import com.estimote.coresdk.service.BeaconManager;
import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.atila.smartvehiclefleet.SettingsActivity.settingsValues;

public class ScanActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Button searchAllButton;
    public static HashMap<Integer,Proximity> proximityValues = new HashMap<>();
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> vehiclesNearbyList;
    private BeaconManager beaconManager;
    private ListView listView;
    private DataProvider dataProvider;
    private TextView scanHeader;
    private BroadcastReceiver broadcastReceiver;
    private Double latitude;
    private Double longitude;
    private Boolean scanning= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        searchAllButton = (Button) findViewById(R.id.searchAllButton);
        listView = (ListView) findViewById(R.id.listView);
        scanHeader = (TextView) findViewById(R.id.scanHeader);
        beaconManager = new BeaconManager(getApplicationContext());
        dataProvider = new DataProvider(this);

        //Checks for location permissions
        if(!runtimePermissions()){
            runtimePermissions();
        }

        String[] vehiclesNearby = new String[]{};
        vehiclesNearbyList = new ArrayList<String>();
        vehiclesNearbyList.addAll(Arrays.asList(vehiclesNearby));
        listAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, vehiclesNearbyList);
        listView.setAdapter( listAdapter );
        //populating the proximity values
        proximityValues.put(0,Proximity.IMMEDIATE);
        proximityValues.put(1,Proximity.NEAR);
        proximityValues.put(2,Proximity.FAR);
        proximityValues.put(3,Proximity.UNKNOWN);
        settingsValues.put(0,"Vehicles within few centimeters");
        settingsValues.put(1,"Vehicles within few meters");
        settingsValues.put(2,"More than a few meters");
        settingsValues.put(3,"Unknown");
        final SharedPreferences prefs = getSharedPreferences(SettingsActivity.MY_PREFS_NAME, MODE_PRIVATE);
        scanHeader.setText(settingsValues.get(prefs.getInt("radius",0)));
        //listener for search all button
        searchAllButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(!scanning){
                    Intent i = new Intent(getApplicationContext(),LocationService.class);
                    startService(i);
                    findVehicles();
                    scanning = true;
                    searchAllButton.setText("Stop Scan");
                }else{
                    beaconManager.disconnect();
                    Intent i = new Intent(getApplicationContext(),LocationService.class);
                    stopService(i);
                    scanning = false;
                    searchAllButton.setText("Scan");
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    latitude = intent.getExtras().getDouble("latitude");
                    longitude = intent.getExtras().getDouble("longitude");
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
        Intent i = new Intent(getApplicationContext(),LocationService.class);
        stopService(i);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            Intent intent = new Intent(ScanActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(ScanActivity.this, OverviewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_scan) {
            Intent intent = new Intent(ScanActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_scan_all) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void findVehicles(){
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                beaconManager.startLocationDiscovery();
            }
        });
        //Loading bar
        final ProgressDialog progress = new ProgressDialog(this);
        final SharedPreferences prefs = getSharedPreferences(SettingsActivity.MY_PREFS_NAME, MODE_PRIVATE);
        progress.setTitle("Loading");
        progress.setMessage("Wait while starting the scanner..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        beaconManager.setLocationListener(new BeaconManager.LocationListener() {
            @Override
            public void onLocationsFound(List<EstimoteLocation> beacons) {
                Integer radius = prefs.getInt("radius",0);
                if(radius==0 && latitude != null) {
                    progress.setTitle("Searching");
                    progress.setMessage("Searching for vehicle ");
                    for (EstimoteLocation beacon : beacons) {
                        if (RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE) {
                            progress.dismiss();
                            //listView.setAdapter(arrayAdapter);
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(!vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    //inserts new location if not exists, else updates
                                    if (!dataProvider.selectLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER))).moveToFirst()){
                                        dataProvider.insertLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString());
                                    }else{
                                        dataProvider.updateLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString());
                                    }
                                    listAdapter.add(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();

                        }else{
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    listAdapter.remove(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }else if(radius==1 && latitude != null) {
                    progress.setTitle("Searching");
                    progress.setMessage("Searching for vehicle ");
                    for (EstimoteLocation beacon : beacons) {
                        if ((RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE ||RegionUtils.computeProximity(beacon) == Proximity.NEAR)) {
                            progress.dismiss();
                            //listView.setAdapter(arrayAdapter);
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(!vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    if (!dataProvider.selectLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER))).moveToFirst()){
                                        dataProvider.insertLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString());
                                    }else{
                                        dataProvider.updateLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString());
                                    }
                                    listAdapter.add(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();

                        }else{
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    listAdapter.remove(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();
                        }

                    }
                }else if(radius==2) {
                    for (EstimoteLocation beacon : beacons) {
                        if ((RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE ||RegionUtils.computeProximity(beacon) == Proximity.NEAR ||RegionUtils.computeProximity(beacon) == Proximity.FAR)) {
                            progress.dismiss();
                            //listView.setAdapter(arrayAdapter);
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(!vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    listAdapter.add(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();

                        }else{
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    listAdapter.remove(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();
                        }

                    }
                }else if(radius==3) {
                    for (EstimoteLocation beacon : beacons) {
                        if ((RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE ||RegionUtils.computeProximity(beacon) == Proximity.NEAR ||RegionUtils.computeProximity(beacon) == Proximity.FAR ||RegionUtils.computeProximity(beacon) == Proximity.UNKNOWN)) {
                            progress.dismiss();
                            //listView.setAdapter(arrayAdapter);
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(!vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    listAdapter.add(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();

                        }else{
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    listAdapter.remove(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
                                }
                                cursor.moveToNext();
                            }
                            cursor.close();
                            listAdapter.notifyDataSetChanged();
                        }

                    }
                }

            }
        });

    }

    private Boolean runtimePermissions() {
        if(Build.VERSION.SDK_INT >=23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},100);
            return true;
        }
        return false;
    }
}
