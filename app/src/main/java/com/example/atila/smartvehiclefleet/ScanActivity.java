package com.example.atila.smartvehiclefleet;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
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
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.RegionUtils;
import com.estimote.coresdk.observation.utils.Proximity;
import com.estimote.coresdk.recognition.packets.EstimoteLocation;
import com.estimote.coresdk.service.BeaconManager;
import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;
import com.example.atila.smartvehiclefleet.services.LocationService;
import com.example.atila.smartvehiclefleet.services.SyncService;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
    private Float accuracy;
    private Boolean scanning= false;
    private SyncService sync;
    private Date date;

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
        sync = new SyncService(getApplicationContext(),ScanActivity.this);

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
                    if(prefs.getBoolean("switch",false)){
                        findVehiclesLog();
                    }else{
                        findVehicles();
                    }
                    scanning = true;
                    searchAllButton.setText("Stop Scan");
                }else{
                    beaconManager.disconnect();
                    Intent i = new Intent(getApplicationContext(),LocationService.class);
                    stopService(i);

                    if(prefs.getBoolean("switch",false)){
                        Cursor cursor2 = dataProvider.selectAllLocationsLogsGrouped();
                        if (cursor2.getCount() >0){
                            while (!cursor2.isAfterLast()) {
                                LatLng estimatedLocation= estimatedLocation(cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER_LOG)));
                                date = new Date();
                                if (!dataProvider.selectLocation(cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER_LOG))).moveToFirst()){
                                    dataProvider.insertLocation(cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER_LOG)),String.valueOf(estimatedLocation.latitude),String.valueOf(estimatedLocation.longitude),estimatedaccuracy(cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER_LOG))), new Timestamp(date.getTime()).toString());
                                }else{
                                    dataProvider.updateLocation(cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER_LOG)),String.valueOf(estimatedLocation.latitude),String.valueOf(estimatedLocation.longitude),estimatedaccuracy(cursor2.getString(cursor2.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER_LOG))), new Timestamp(date.getTime()).toString());
                                }
                                cursor2.moveToNext();
                            }
                            cursor2.close();
                            sync.postData();
                            sync.postDataLog();
                        }
                    }else{
                        Cursor cursor2 = dataProvider.selectAllLocations();
                        if (cursor2.getCount() >0){
                            sync.postData();
                        }
                    }

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
                    accuracy = intent.getExtras().getFloat("accuracy");
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
        } else if (id == R.id.nav_overview) {
            Intent intent = new Intent(ScanActivity.this, MapsActivity.class);
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
                            date = new Date();
                            progress.dismiss();
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(!vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    //inserts new location if not exists, else updates
                                    if (!dataProvider.selectLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER))).moveToFirst()){
                                        dataProvider.insertLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy, new Timestamp(date.getTime()).toString());
                                    }else{
                                        dataProvider.updateLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy, new Timestamp(date.getTime()).toString());
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
                            date = new Date();
                            progress.dismiss();
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(!vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    if (!dataProvider.selectLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER))).moveToFirst()){
                                        dataProvider.insertLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy,new Timestamp(date.getTime()).toString());
                                    }else{
                                        dataProvider.updateLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy,new Timestamp(date.getTime()).toString());
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
                }else if(radius==2 && latitude != null) {
                    progress.setTitle("Searching");
                    progress.setMessage("Searching for vehicle ");
                    for (EstimoteLocation beacon : beacons) {
                        if ((RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE ||RegionUtils.computeProximity(beacon) == Proximity.NEAR ||RegionUtils.computeProximity(beacon) == Proximity.FAR)) {
                            date = new Date();
                            progress.dismiss();
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                if(!vehiclesNearbyList.contains(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)))){
                                    if (!dataProvider.selectLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER))).moveToFirst()){
                                        dataProvider.insertLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy,new Timestamp(date.getTime()).toString());
                                    }else{
                                        dataProvider.updateLocation(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy,new Timestamp(date.getTime()).toString());
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
                }else if(radius==3) {
                    for (EstimoteLocation beacon : beacons) {
                        if ((RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE ||RegionUtils.computeProximity(beacon) == Proximity.NEAR ||RegionUtils.computeProximity(beacon) == Proximity.FAR ||RegionUtils.computeProximity(beacon) == Proximity.UNKNOWN)) {
                            date = new Date();
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
    //this piece of code is for the log experiment
    public void findVehiclesLog(){
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
                            date = new Date();
                            progress.dismiss();
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                dataProvider.insertLocationLog(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy, new Timestamp(date.getTime()).toString());
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
                }else if(radius==1 && latitude != null) {
                    progress.setTitle("Searching");
                    progress.setMessage("Searching for vehicle ");
                    for (EstimoteLocation beacon : beacons) {
                        if ((RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE ||RegionUtils.computeProximity(beacon) == Proximity.NEAR)) {
                            date = new Date();
                            progress.dismiss();
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                dataProvider.insertLocationLog(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy,new Timestamp(date.getTime()).toString());
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
                }else if(radius==2 && latitude != null) {
                    progress.setTitle("Searching");
                    progress.setMessage("Searching for vehicle ");
                    for (EstimoteLocation beacon : beacons) {
                        if ((RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE ||RegionUtils.computeProximity(beacon) == Proximity.NEAR ||RegionUtils.computeProximity(beacon) == Proximity.FAR)) {
                            date = new Date();
                            progress.dismiss();
                            Cursor cursor = dataProvider.selectVehicleIdentifier(beacon.id.toString());
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                dataProvider.insertLocationLog(cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)),latitude.toString(),longitude.toString(),accuracy,new Timestamp(date.getTime()).toString());
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
                            date = new Date();
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
    //Location estimation using median
    public LatLng estimatedLocation(String vehicle){
        ArrayList<Double> lat = new ArrayList<Double>();
        ArrayList<Double> lon = new ArrayList<Double>();
        Cursor latestlocations = dataProvider.selectLatestLocationLog(vehicle);
        while (!latestlocations.isAfterLast()) {
            lat.add(Double.parseDouble(latestlocations.getString(latestlocations.getColumnIndex(DbHelper.LATITUDE_LOG))));
            lon.add(Double.parseDouble(latestlocations.getString(latestlocations.getColumnIndex(DbHelper.LONGITUDE_LOG))));
            latestlocations.moveToNext();
        }
        latestlocations.close();
        Collections.sort(lat);
        Collections.sort(lon);
        Double medianLat = median(lat);
        Double medianLon = median(lon);

        return new LatLng(medianLat,medianLon);
    }
    //Accuracy calculation using sd
    public Float estimatedaccuracy(String vehicle){
        ArrayList<Double> lat = new ArrayList<Double>();
        ArrayList<Double> lon = new ArrayList<Double>();
        Cursor latestlocations = dataProvider.selectLatestLocationLog(vehicle);
        while (!latestlocations.isAfterLast()) {
            lat.add(Double.parseDouble(latestlocations.getString(latestlocations.getColumnIndex(DbHelper.LATITUDE_LOG))));
            lon.add(Double.parseDouble(latestlocations.getString(latestlocations.getColumnIndex(DbHelper.LONGITUDE_LOG))));
            latestlocations.moveToNext();
        }
        latestlocations.close();
        Double sdLat = standardDeviation(lat);
        Double sdLon = standardDeviation(lon);
        LatLng estimatedLoc = estimatedLocation(vehicle);
        Location loc1 = new Location("");
        loc1.setLatitude(estimatedLoc.latitude);
        loc1.setLongitude(estimatedLoc.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(estimatedLoc.latitude+sdLat);
        loc2.setLongitude(estimatedLoc.longitude+sdLon);

        return loc1.distanceTo(loc2);
    }

    public Double median(List<Double> a){
        int middle = a.size()/2;

        if (a.size() % 2 == 1) {
            return a.get(middle);
        } else {
            return (a.get(middle-1) + a.get(middle)) / 2.0;
        }
    }

    public Double sum (List<Double> a){
        if (a.size() > 0) {
            Double sum = 0.0;

            for (Double i : a) {
                sum += i;
            }
            return sum;
        }
        return 0.0;
    }

    public double mean (List<Double> a){
        Double sum = sum(a);
        Double mean = 0.0;
        mean = sum / (a.size() * 1.0);
        return mean;
    }

    public Double standardDeviation (List<Double> a){
        Double sum = 0.0;
        Double mean = mean(a);

        for (Double i : a)
            sum += Math.pow((i - mean), 2);
        return Math.sqrt( sum / ( a.size() - 1 ) );
    }
}
