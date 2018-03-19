package com.example.atila.smartvehiclefleet;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;
import com.example.atila.smartvehiclefleet.dbhelper.DbHelper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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
    private List<Double> accuracy = new ArrayList<Double>();
    private LatLng spaceBCsoutheast;
    private LatLng spaceAsoutheast;
    private LatLng spaceDsoutheast;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //sets the south west and north east corners of the image
        spaceBCsoutheast = new LatLng(55.7254616764489,12.380295853045254);
        spaceAsoutheast = new LatLng(55.72530603133699, 12.381780019049074);
        spaceDsoutheast = new LatLng(55.726561249837005, 12.38076346183243);
        dataProvider = new DataProvider(this);
        Cursor cursor = dataProvider.selectAllLocations();

        if (cursor != null){
            while (!cursor.isAfterLast()) {
                tempId = cursor.getString(cursor.getColumnIndex(DbHelper.REF_VEHICLE_IDENTIFIER))+" ("+cursor.getFloat(cursor.getColumnIndex(DbHelper.ACCURACY))+")";
                vehicleIds.add(tempId);
                loc = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.LATITUDE))),Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.LONGITUDE))));
                points.add(loc);
                accuracy.add(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DbHelper.ACCURACY))));
                cursor.moveToNext();
            }
        }

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
        for (int i = 0 ; i < points.size(); i++){
            mMap.addMarker(new MarkerOptions().position(points.get(i)).title(vehicleIds.get(i)).icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("marker",48,48))).zIndex(2));
            mMap.addCircle(new CircleOptions().center(points.get(i)).radius(accuracy.get(i)).zIndex(2).strokeColor(Color.RED).strokeWidth(1).fillColor(0x22FE2E2E));
        };

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(points.get(0))
                .zoom(17)
                .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(update);
        //listener for map clicks
        /*
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });*/
    }

    public Bitmap resizeBitmap(String drawableName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }


}
