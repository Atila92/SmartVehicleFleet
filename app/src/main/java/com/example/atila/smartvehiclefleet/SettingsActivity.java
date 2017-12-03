package com.example.atila.smartvehiclefleet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private SeekBar seekBar;
    private TextView seekBarHeader;
    public static final String MY_PREFS_NAME = "settingsFile";
    public static HashMap<Integer,String> settingsValues = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBarHeader = (TextView) findViewById(R.id.seekBarHeader);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //populating the map with values
        settingsValues.put(0,"Vehicles within few centimeters");
        settingsValues.put(1,"Vehicles within few meters");
        settingsValues.put(2,"More than a few meters");
        settingsValues.put(3,"Unknown");

        //Setting up access to shared prefs
        final SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        final SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        //Integer radiusSetting;
        seekBar.setProgress(prefs.getInt("radius",0));
        seekBarHeader.setText(settingsValues.get(prefs.getInt("radius",0)));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt("radius",seekBar.getProgress());
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if(progress==0){
                    seekBarHeader.setText("Up to a few centimeters");
                }
                else if(progress==1){
                    seekBarHeader.setText("Up to a few meters");
                }
                else if(progress==2){
                    seekBarHeader.setText("More than a few meters");
                }
                else if(progress==3){
                    seekBarHeader.setText("Unknown");
                }

            }
        });

        }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_scan) {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_scan_all) {
            Intent intent = new Intent(SettingsActivity.this, ScanActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
