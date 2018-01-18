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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class OverviewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> vehicleLocationsList;
    private DataProvider dataProvider;

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
}