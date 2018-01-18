package com.example.atila.smartvehiclefleet;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static String vehicleId;
    private DataProvider dataProvider;
    private LatLng loc;
    private String tempId;
    private List<LatLng> points = new ArrayList<LatLng>();
    private List<String> vehicleIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        dataProvider = new DataProvider(this);
        Cursor cursor = dataProvider.selectAllLocations();

        if (cursor != null){
            while (!cursor.isAfterLast()) {
                tempId = cursor.getString(cursor.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER));
                vehicleIds.add(tempId);
                loc = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.LATITUDE))),Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.LONGITUDE))));
                points.add(loc);
                cursor.moveToNext();
            }
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in location
        for (int i = 0 ; i < points.size(); i++){
            mMap.addMarker(new MarkerOptions().position(points.get(i)).title(vehicleIds.get(i)));
        };
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(points.get(0))
                .zoom(17)
                .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(update);
    }
}
