package com.example.atila.smartvehiclefleet;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static String vehicleId;
    private DataProvider dataProvider;
    private LatLng loc;
    private String tempId;
    private List<LatLng> points = new ArrayList<LatLng>();
    private List<String> vehicleIds = new ArrayList<String>();
    private List<Double> accuracy = new ArrayList<Double>();
    private LatLng spaceBCsoutheast;
    private LatLng spaceAsoutheast;
    private LatLng spaceDsoutheast;
    private EditText editTextSearchMap;
    private HashMap<String,LatLng> vehicleLocations = new HashMap<>();
    private HashMap<String,Marker> vehicleMarkers = new HashMap<>();
    private HashMap<String,Circle> vehicleCirkles = new HashMap<>();
    private Boolean infoShown = false;
    private String infoShownMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        editTextSearchMap = (EditText) findViewById(R.id.editTextSearchMap);
        //sets the south west and north east corners of the image
        spaceBCsoutheast = new LatLng(55.7254616764489,12.380295853045254);
        spaceAsoutheast = new LatLng(55.72530603133699, 12.381780019049074);
        spaceDsoutheast = new LatLng(55.726561249837005, 12.38076346183243);
        dataProvider = new DataProvider(this);
        Cursor cursor = dataProvider.selectAllLocations();

        if (cursor.getCount()>0){
            while (!cursor.isAfterLast()) {
                tempId = cursor.getString(cursor.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER)).toUpperCase();
                vehicleIds.add(tempId);
                loc = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.LATITUDE))),Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.LONGITUDE))));
                points.add(loc);
                accuracy.add(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.ACCURACY))));
                vehicleLocations.put(tempId,loc);
                cursor.moveToNext();
            }
        }

        //Listener for search bar enter click
        editTextSearchMap.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(vehicleIds.contains(editTextSearchMap.getText().toString().toUpperCase())){
                        defaultCameraPosition();
                        zoomVehicleInFocus(editTextSearchMap.getText().toString().toUpperCase());

                    }else{
                        if (infoShown) {
                            vehicleMarkers.get(infoShownMarker).hideInfoWindow();
                            infoShown = false;
                        }
                        defaultCameraPosition();
                        Toast.makeText(MapsActivity.this,"Vehicle not found!",Toast.LENGTH_LONG).show();
                    }

                }
                return false;
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMyLocationEnabled(true);
        //Add overlay
        GroundOverlayOptions bilsalgMapSpaceA = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.space_a)).anchor(1,1).position(spaceAsoutheast,94.3f,88.86f).bearing(12f).transparency(0.1f).zIndex(1);
        GroundOverlayOptions bilsalgMapSpaceBC = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.space_bc)).anchor(1,1).position(spaceBCsoutheast,105.4f,125f).bearing(12f).transparency(0.1f).zIndex(1);
        GroundOverlayOptions bilsalgMapSpaceD = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.space_d)).anchor(1,1).position(spaceDsoutheast,108.7f,152.3f).bearing(12f).transparency(0.1f).zIndex(1);
        mMap.addGroundOverlay(bilsalgMapSpaceA);
        mMap.addGroundOverlay(bilsalgMapSpaceBC);
        mMap.addGroundOverlay(bilsalgMapSpaceD);
        // Add a marker in location and accuracy cirkle

        defaultCameraPosition();
    }

    public Bitmap resizeBitmap(String drawableName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    public void defaultCameraPosition(){

        for (int i = 0 ; i < points.size(); i++){
            if(!vehicleMarkers.containsKey(vehicleIds.get(i))){
                Marker marker = mMap.addMarker(new MarkerOptions().position(points.get(i)).title(vehicleIds.get(i)).icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("marker",48,48))).zIndex(2));
                vehicleMarkers.put(vehicleIds.get(i), marker);
                Circle cirkle = mMap.addCircle(new CircleOptions().center(points.get(i)).radius(accuracy.get(i)).zIndex(2).strokeColor(Color.RED).strokeWidth(1).fillColor(0x22FE2E2E));
                vehicleCirkles.put(vehicleIds.get(i),cirkle);
            }
        };

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(points.get(0))
                .zoom(17)
                .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(update);
    }

    public void zoomVehicleInFocus(String vehicle){

        Iterator it = vehicleMarkers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,Marker> pair = (Map.Entry)it.next();

            if(!pair.getKey().toUpperCase().equals(vehicle)){
                pair.getValue().remove();
                it.remove();
            }

        }

        Iterator it2 = vehicleCirkles.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<String,Circle> pair = (Map.Entry)it2.next();

            if(!pair.getKey().toUpperCase().equals(vehicle)){
                pair.getValue().remove();
                it2.remove();
            }

        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(vehicleLocations.get(vehicle))
                .zoom(19)
                .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(update);
        infoShownMarker = editTextSearchMap.getText().toString().toUpperCase();
        vehicleMarkers.get(infoShownMarker).showInfoWindow();
        infoShown = true;
    }


}
