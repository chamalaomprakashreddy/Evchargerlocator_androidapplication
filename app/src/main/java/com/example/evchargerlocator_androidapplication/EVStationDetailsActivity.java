package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EVStationDetailsActivity extends AppCompatActivity {

    private TextView stationName, stationAddress;
    private LatLng stationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evstation_details);

        stationName = findViewById(R.id.station_name);
        stationAddress = findViewById(R.id.station_address);

        // Get Data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            stationName.setText(intent.getStringExtra("station_name"));
            stationLocation = intent.getParcelableExtra("station_location");

            // Fetch Address (Reverse Geocoding)
            getAddressFromLatLng(stationLocation);
        }
    }

    private void getAddressFromLatLng(LatLng location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (!addresses.isEmpty()) {
                stationAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                stationAddress.setText("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            stationAddress.setText("Error fetching address");
        }
    }
}
