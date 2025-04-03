package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class StationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "StationDetailsActivity";

    private TextView stationName, stationAddress, distanceText, timeText;
    private TextView accessType, accessTime, paymentMethods, fullAddress;
    private TextView plugType, plugPrice, plugAvailability, lastUsed;
    private TextView infoDistance, infoDriveTime;
    private Button startChargingBtn;
    private ImageButton navButton, backBtn;

    private String finalAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        // Bind views
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
        startChargingBtn = findViewById(R.id.startChargingBtn);
        navButton = findViewById(R.id.navigationBtn);
        View backArrowText = findViewById(R.id.backArrowText);

        // Get data from Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("stationName");
        String payment = intent.getStringExtra("paymentMethods");
        String plug = intent.getStringExtra("plugType");
        String price = intent.getStringExtra("plugPrice");
        String availability = intent.getStringExtra("plugAvailability");
        String distance = intent.getStringExtra("distance");
        String duration = intent.getStringExtra("duration");
        String address = intent.getStringExtra("address");
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);

        // Use provided address or fallback to Geocoder
        finalAddress = address != null && !address.isEmpty() ? address : getAddressFromLatLng(latitude, longitude);
        if (finalAddress == null || finalAddress.isEmpty()) {
            finalAddress = "Address not available";
        }

        // Populate data
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
        plugPrice.setText(price != null ? price : "$0.40/kWh");
        plugAvailability.setText(availability != null ? availability : "2/2 Available");
        lastUsed.setText("Last used 2 days ago");

        // Google Maps Navigation
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

        // Start charging
        startChargingBtn.setOnClickListener(v ->
                startActivity(new Intent(this, PaymentActivity.class))
        );
    }

    private String getAddressFromLatLng(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lng, 1);
            if (list != null && !list.isEmpty()) {
                Address address = list.get(0);
                return address.getAddressLine(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoder failed", e);
        }
        return null;
    }
}