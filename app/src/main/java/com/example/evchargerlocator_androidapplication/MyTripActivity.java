package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyTripActivity extends AppCompatActivity {

    private ArrayList<ChargingStation> stations;
    private LinearLayout tripContainer;
    private LatLng startLatLng;
    private LatLng endLatLng;
    private String fromAddress, toAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_trip);

        tripContainer = findViewById(R.id.myTripContainer);

        // ✅ Safely retrieve data from intent
        stations = getIntent().getParcelableArrayListExtra("stations");
        startLatLng = parseLatLng(getIntent().getStringExtra("startLocation"));
        endLatLng = parseLatLng(getIntent().getStringExtra("endLocation"));
        fromAddress = getIntent().getStringExtra("fromAddress");
        toAddress = getIntent().getStringExtra("toAddress");

        // ✅ Render trip timeline visually
        if (stations != null && startLatLng != null && endLatLng != null) {
            renderTripTimeline(stations);
        }

        // ✅ Back button functionality
        ImageButton backBtn = findViewById(R.id.backButton);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MyTripActivity.this, HomePageActivity.class);
                intent.putParcelableArrayListExtra("stations", stations);
                intent.putExtra("startLocation", startLatLng.latitude + "," + startLatLng.longitude);
                intent.putExtra("endLocation", endLatLng.latitude + "," + endLatLng.longitude);
                intent.putExtra("fromAddress", fromAddress);
                intent.putExtra("toAddress", toAddress);
                intent.putExtra("restoreTrip", true);
                startActivity(intent);
                finish();
            });
        }
    }

    private void renderTripTimeline(List<ChargingStation> tripStations) {
        tripContainer.removeAllViews();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // From Address
        TextView fromView = new TextView(this);
        fromView.setText("From\n" + (fromAddress != null ? fromAddress : getAddressFromLatLng(startLatLng, geocoder)));
        fromView.setTextSize(16f);
        fromView.setPadding(16, 16, 16, 16);
        fromView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tripContainer.addView(fromView);

        LatLng previous = startLatLng;

        // Intermediate Stations
        for (int i = 0; i < tripStations.size(); i++) {
            ChargingStation station = tripStations.get(i);
            LatLng latLng = new LatLng(station.getLatitude(), station.getLongitude());

            View connector = getLayoutInflater().inflate(R.layout.connector_timeline, tripContainer, false);
            TextView distText = connector.findViewById(R.id.connectorDistance);
            double dist = SphericalUtil.computeDistanceBetween(previous, latLng) / 1609.34;
            distText.setText((i == 0 ? "Distance from start: " : "Distance from previous: ") + String.format("%.2f mi", dist));
            tripContainer.addView(connector);

            View card = getLayoutInflater().inflate(R.layout.item_trip_segment, tripContainer, false);
            ((TextView) card.findViewById(R.id.stationName)).setText(station.getName());
            ((TextView) card.findViewById(R.id.stationDistance)).setText(String.format("%.2f mi", dist));
            tripContainer.addView(card);

            previous = latLng;
        }

        // To Address
        if (endLatLng != null) {
            View connector = getLayoutInflater().inflate(R.layout.connector_timeline, tripContainer, false);
            TextView distText = connector.findViewById(R.id.connectorDistance);
            double dist = SphericalUtil.computeDistanceBetween(previous, endLatLng) / 1609.34;
            distText.setText("Distance to destination: " + String.format("%.2f mi", dist));
            tripContainer.addView(connector);

            TextView toView = new TextView(this);
            toView.setText("To\n" + (toAddress != null ? toAddress : getAddressFromLatLng(endLatLng, geocoder)));
            toView.setTextSize(16f);
            toView.setPadding(16, 16, 16, 16);
            toView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            tripContainer.addView(toView);
        }
    }

    private LatLng parseLatLng(String locStr) {
        if (locStr == null || !locStr.contains(",")) return null;
        String[] parts = locStr.split(",");
        try {
            return new LatLng(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getAddressFromLatLng(LatLng latLng, Geocoder geocoder) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) return addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latLng.latitude + ", " + latLng.longitude;
    }
}
