package com.example.atila.smartvehiclefleet;

import android.app.Application;

import com.estimote.coresdk.common.config.EstimoteSDK;

/**
 * Created by Atila on 09-Oct-17.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: put your App ID and App Token here
        // You can get them by adding your app on https://cloud.estimote.com/#/apps
        EstimoteSDK.initialize(getApplicationContext(), "vehiclebeaconprototype-j17", "907e4bf380bf666da2634cf7c8fbec02");

        // uncomment to enable debug-level logging
        // it's usually only a good idea when troubleshooting issues with the Estimote SDK
//        EstimoteSDK.enableDebugLogging(true);");
    }
}
