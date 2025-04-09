package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.List;
import java.util.Locale;

public class StationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "StationDetailsActivity";

    private TextView stationName, stationAddress, distanceText, timeText;
    private TextView accessType, accessTime, paymentMethods, fullAddress;
    private TextView plugType, plugPrice, plugAvailability, lastUsed;
    private TextView infoDistance, infoDriveTime, plugLevel;
    private Button startChargingBtn;
    private ImageButton navButton;
    private View backArrowText;

    private String finalAddress = "";
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        // View bindings
        stationName = findViewById(R.id.detailStationName);
        stationAddress = findViewById(R.id.detailStationAddress);
        distanceText = findViewById(R.id.detailDistanceText);
        timeText = findViewById(R.id.detailDurationText);
        accessType = findViewById(R.id.accessTypeText);
        accessTime = findViewById(R.id.accessTimeText);
        paymentMethods = findViewById(R.id.paymentMethodText);
        fullAddress = findViewById(R.id.fullAddressText);
        plugType = findViewById(R.id.plugTypeText);
        plugPrice = findViewById(R.id.plugPriceText);
        plugAvailability = findViewById(R.id.plugAvailabilityText);
        lastUsed = findViewById(R.id.lastUsedText);
        infoDistance = findViewById(R.id.infoDistance);
        infoDriveTime = findViewById(R.id.infoDriveTime);
        plugLevel = findViewById(R.id.plugLevelText);
        startChargingBtn = findViewById(R.id.startChargingBtn);
        navButton = findViewById(R.id.navigationBtn);
        backArrowText = findViewById(R.id.backArrowText);

        // Get data from Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("stationName");
        String payment = intent.getStringExtra("paymentMethods");
        String plug = intent.getStringExtra("plugType");
        String distance = intent.getStringExtra("distance");
        String duration = intent.getStringExtra("duration");
        String address = intent.getStringExtra("address");
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);

        // Address fallback
        finalAddress = (address != null && !address.isEmpty())
                ? address
                : getAddressFromLatLng(latitude, longitude);
        if (finalAddress == null || finalAddress.isEmpty()) {
            finalAddress = "Address not available";
        }

        // Static display
        stationName.setText(name != null ? name : "EV Station");
        stationAddress.setText(finalAddress);
        fullAddress.setText(finalAddress);
        distanceText.setText("🚗 " + (distance != null ? distance : "N/A"));
        timeText.setText(duration != null ? duration : "N/A");
        infoDistance.setText(distance != null ? distance : "N/A");
        infoDriveTime.setText(duration != null ? duration : "N/A");

        accessType.setText("Public");
        accessTime.setText("24 Hours");
        paymentMethods.setText(payment != null ? payment : "Google Pay, PayPal, Credit/Debit");
        plugType.setText(plug != null ? plug : "J1772");
        lastUsed.setText("Last used 2 days ago");

        // Fetch from Firebase
        if (name != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference("ChargingStations");
            databaseRef.orderByChild("name").equalTo(name)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    ChargingStation station = child.getValue(ChargingStation.class);
                                    if (station != null) {
                                        plugLevel.setText(station.getChargingLevel() != null ? station.getChargingLevel() : "Unknown");
                                        plugAvailability.setText(station.getAvailability() != null ? station.getAvailability() : "N/A");

                                        int price = station.getPricing(); // In cents
                                        if (price > 0) {
                                            plugPrice.setText(String.format(Locale.getDefault(), "$%.2f/kWh", price / 100.0));
                                        } else {
                                            plugPrice.setText("Not Available");
                                        }
                                    }
                                }
                            } else {
                                setDefaultDBValues();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e(TAG, "Database error", error.toException());
                            setDefaultDBValues();
                        }
                    });
        } else {
            setDefaultDBValues();
        }

        // Google Maps navigation
        navButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        // Back button
        backArrowText.setOnClickListener(v -> onBackPressed());

        // Launch PaymentActivity
        startChargingBtn.setOnClickListener(v ->
                startActivity(new Intent(this, PaymentActivity.class))
        );
    }

    private void setDefaultDBValues() {
        plugLevel.setText("Unknown");
        plugAvailability.setText("N/A");
        plugPrice.setText("Not Available");
    }

    private String getAddressFromLatLng(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lng, 1);
            if (list != null && !list.isEmpty()) {
                return list.get(0).getAddressLine(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoder failed", e);
        }
        return null;
    }
}
