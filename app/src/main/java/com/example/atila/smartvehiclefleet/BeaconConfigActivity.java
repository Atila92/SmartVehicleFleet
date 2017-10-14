package com.example.atila.smartvehiclefleet;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.recognition.utils.DeviceId;
import com.example.atila.smartvehiclefleet.dbhelper.DataProvider;

import java.nio.charset.Charset;

public class BeaconConfigActivity extends AppCompatActivity {

    private String configBeacon = "";
    private TextView displayBeaconIdentifierTextView;
    private EditText editText1;
    private final String toastMessageSuccess = "Setup done!";
    private final String toastMessageInput = "Please enter a valid vehicle identifier!";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_config);
        displayBeaconIdentifierTextView = (TextView) findViewById(R.id.displayBeaconIdentifierTextView);
        editText1 = (EditText) findViewById(R.id.editText1);
        final Handler handler = new Handler();

        final DataProvider dataProvider = new DataProvider(this);
        //provides a hint in the input field
        editText1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Always use a TextKeyListener when clearing a TextView to prevent android
                    // warnings in the log
                    editText1.setHint("Enter vehicle identifier here..");

                }
            }
        });

        //Listener for enter click
        editText1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(editText1.getText().toString().length()>4) {
                        //It inputs data as a new field if it doesn't already exist
                        if (!dataProvider.selectVehicleIdentifier(configBeacon).moveToFirst()) {
                            dataProvider.insertMapping(configBeacon, editText1.getText().toString());
                            Toast.makeText(BeaconConfigActivity.this, toastMessageSuccess, Toast.LENGTH_SHORT).show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(BeaconConfigActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }, 1500);
                        } else {
                            dataProvider.updateMapping(configBeacon, editText1.getText().toString());
                            Toast.makeText(BeaconConfigActivity.this, toastMessageSuccess, Toast.LENGTH_SHORT).show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(BeaconConfigActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }, 1500);
                        }

                    }else{
                        Toast.makeText(BeaconConfigActivity.this, toastMessageInput, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                for (int i = 0; i < rawMsgs.length; i++) {
                    NdefMessage msg = (NdefMessage) rawMsgs[i];
                    DeviceId beaconId = findBeaconId(msg);
                    if (beaconId != null) {
                        displayBeaconIdentifierTextView.setText("Configuring beacon with id: "+beaconId.toString());
                        configBeacon = beaconId.toString();
                    }
                }
            }
        }
    }

    private static DeviceId findBeaconId(NdefMessage msg) {
        NdefRecord[] records = msg.getRecords();
        for (NdefRecord record : records) {
            if (record.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE) {
                String type = new String(record.getType(), Charset.forName("ascii"));
                if ("estimote.com:id".equals(type)) {
                    return DeviceId.fromBytes(record.getPayload());
                }
            }
        }
        return null;
    }
}
