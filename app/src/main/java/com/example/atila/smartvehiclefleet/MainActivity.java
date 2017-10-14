package com.example.atila.smartvehiclefleet;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView firstTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //For test of db
        firstTextView = (TextView) findViewById(R.id.firstTextView);
        DataProvider dataProvider = new DataProvider(this);

        firstTextView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("Click---------", "Working!");

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Cursor cursor = dataProvider.selectAllMappings();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            firstTextView.setText(cursor.getString(cursor.getColumnIndex(DbHelper.MAPPING_ID))+":"+cursor.getString(cursor.getColumnIndex(DbHelper.BEACON_IDENTIFIER))
                    +":"+cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));

            Log.d("Click---------", cursor.getString(cursor.getColumnIndex(DbHelper.MAPPING_ID))+":"+cursor.getString(cursor.getColumnIndex(DbHelper.BEACON_IDENTIFIER))
                    +":"+cursor.getString(cursor.getColumnIndex(DbHelper.VEHICLE_IDENTIFIER)));
            cursor.moveToNext();
            }
            cursor.close();

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
            // Handle the camera action
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
