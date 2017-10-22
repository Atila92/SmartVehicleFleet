package com.example.atila.smartvehiclefleet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.cloud.api.CloudCallback;
import com.estimote.coresdk.cloud.api.EstimoteCloud;
import com.estimote.coresdk.cloud.model.BeaconInfo;
import com.estimote.coresdk.common.exception.EstimoteCloudException;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.RegionUtils;
import com.estimote.coresdk.observation.utils.Proximity;
import com.estimote.coresdk.recognition.packets.EstimoteLocation;
import com.estimote.coresdk.service.BeaconManager;
import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView textViewSearchHeader;
    private EditText editTextSearch;
    private Button searchButton;
    private BeaconManager beaconManager;
    private boolean notificationAlreadyShown = false;
    private DataProvider dataProvider;
    private String beaconIdentifier;
    private String userInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //For test of db
        textViewSearchHeader = (TextView) findViewById(R.id.textViewSearchHeader);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        searchButton = (Button) findViewById(R.id.searchButton);
        beaconManager = new BeaconManager(getApplicationContext());
        dataProvider = new DataProvider(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Listener for enter click
        editTextSearch.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    notificationAlreadyShown = false;
                    beaconManager.disconnect();
                    findVehicle();
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notificationAlreadyShown = false;
                beaconManager.disconnect();
                findVehicle();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_scan) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showNotification(String title, String message) {
        if (notificationAlreadyShown) { return; }

        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
        notificationAlreadyShown = true;
    }

    public void findVehicle(){
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                beaconManager.startLocationDiscovery();
            }
        });
        //Loading bar
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while starting the scanner..");
        progress.setCancelable(true); // disable dismiss by tapping outside of the dialog
        progress.show();
        //Finds the beaconId corresponding to the vehicle id entered
        userInput = editTextSearch.getText().toString();
        Log.d("Userinput --->",userInput);
        if(dataProvider.selectBeaconIdentifier(userInput).moveToFirst()){
            Cursor cursor = dataProvider.selectBeaconIdentifier(userInput);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                beaconIdentifier = cursor.getString(cursor.getColumnIndex(DbHelper.BEACON_IDENTIFIER));
                cursor.moveToNext();
            }
            cursor.close();
            Log.d("beaconid --->",beaconIdentifier);
            beaconManager.setLocationListener(new BeaconManager.LocationListener() {
                @Override
                public void onLocationsFound(List<EstimoteLocation> beacons) {
                    progress.setTitle("Searching");
                    progress.setMessage("Searching for vehicle ");
                    //String beaconId = "[7c8259db97a28a6609b5da060954ef11]";

                    for (EstimoteLocation beacon : beacons) {
                        if (beacon.id.toString().equals(beaconIdentifier) && RegionUtils.computeProximity(beacon) == Proximity.IMMEDIATE) {
                            progress.dismiss();
                            showNotification("Vehicle Found!", "Vehicle with identifier "+userInput+" was found.");
                        }

                    }
                }
            });
            beaconManager.disconnect();
        }else{
            Toast.makeText(MainActivity.this, "Please enter a valid vehicle identifier", Toast.LENGTH_SHORT).show();
            progress.dismiss();
        }


    }
}
