package com.example.evchargerlocator_androidapplication;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ViewStationActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_station);

        // Initialize views
        TextView stationName = findViewById(R.id.viewStationName);
        TextView stationLocation = findViewById(R.id.viewStationLocation);
        TextView powerOutput = findViewById(R.id.viewPowerOutput);
        TextView availability = findViewById(R.id.viewAvailability);
        TextView chargingLevel = findViewById(R.id.viewChargingLevel);
        TextView connectorType = findViewById(R.id.viewConnectorType);
        TextView network = findViewById(R.id.viewNetwork);
        TextView pricing = findViewById(R.id.viewPricing);
        TextView backArrow = findViewById(R.id.back_arrow_view);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        // Get station ID from intent
        String stationId = getIntent().getStringExtra("stationId");

        if (stationId != null) {
            // Fetch station details from Firebase
            databaseReference.child(stationId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ChargingStation station = snapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        // Update UI with fetched data
                        stationName.setText(station.getName());
                        // Convert lat/lng to address
                        String address = getAddressFromLatLng(station.getLatitude(), station.getLongitude());
                        stationLocation.setText(address);
                        powerOutput.setText(station.getPowerOutput());
                        availability.setText(station.getAvailability());
                        chargingLevel.setText(station.getChargingLevel());
                        connectorType.setText(station.getConnectorType());
                        network.setText(station.getNetwork());
                        pricing.setText(String.format("$%.2f", station.getPricing()));
                    } else {
                        Toast.makeText(ViewStationActivity.this, "Station not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewStationActivity.this, "Failed to load station details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Invalid station ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Back button
        backArrow.setOnClickListener(v -> finish());
    }

    // Method to convert latitude and longitude to address
    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressString = new StringBuilder();
                // Build a concise address (e.g., street, city, country)
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressString.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressString.append(", ");
                    }
                }
                return addressString.toString();
            } else {
                return "Address not found";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to fetch address";
        }
    }
}