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
        Button navButton = findViewById(R.id.navigateButton);

        try {
            stations = getIntent().getParcelableArrayListExtra("stations");
            startLatLng = parseLatLng(getIntent().getStringExtra("startLocation"));
            endLatLng = parseLatLng(getIntent().getStringExtra("endLocation"));
            fromAddress = getIntent().getStringExtra("fromAddress");
            toAddress = getIntent().getStringExtra("toAddress");

            Log.d("MyTripActivity", "Start: " + startLatLng + " End: " + endLatLng +
                    " Stations: " + (stations != null ? stations.size() : 0));

            if (stations != null && !stations.isEmpty() && startLatLng != null && endLatLng != null) {
                renderTripTimeline(stations);
            } else {
                Toast.makeText(this, "Missing or empty trip data", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("MyTripActivity", "Error initializing trip", e);
            Toast.makeText(this, "Failed to load trip data", Toast.LENGTH_SHORT).show();
        }

        ImageButton backBtn = findViewById(R.id.backButton);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MyTripActivity.this, HomePageActivity.class);
                intent.putParcelableArrayListExtra("stations", stations);
                intent.putExtra("startLocation", formatLatLng(startLatLng));
                intent.putExtra("endLocation", formatLatLng(endLatLng));
                intent.putExtra("fromAddress", fromAddress);
                intent.putExtra("toAddress", toAddress);
                intent.putExtra("restoreTrip", true);
                startActivity(intent);
                finish();
            });
        }

        if (navButton != null) {
            navButton.setOnClickListener(v -> openGoogleMapsNavigation());
        }
    }

    private void renderTripTimeline(List<ChargingStation> tripStations) {
        tripContainer.removeAllViews();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        TextView fromView = new TextView(this);
        fromView.setText("From\n" + (fromAddress != null ? fromAddress : getAddressFromLatLng(startLatLng, geocoder)));
        fromView.setTextSize(16f);
        fromView.setPadding(16, 16, 16, 16);
        fromView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tripContainer.addView(fromView);

        LatLng previous = startLatLng;

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

    private void openGoogleMapsNavigation() {
        if (startLatLng == null || endLatLng == null) {
            Toast.makeText(this, "Missing start or end location", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder uri = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        uri.append("&origin=").append(formatLatLng(startLatLng));
        uri.append("&destination=").append(formatLatLng(endLatLng));

        if (stations != null && !stations.isEmpty()) {
            uri.append("&waypoints=");
            for (int i = 0; i < stations.size(); i++) {
                ChargingStation s = stations.get(i);
                uri.append(s.getLatitude()).append(",").append(s.getLongitude());
                if (i < stations.size() - 1) uri.append("|");
            }
        }

        uri.append("&travelmode=driving");

        try {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) {
            Log.e("Navigation", "Google Maps not available", e);
            Toast.makeText(this, "Unable to open Google Maps", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng parseLatLng(String locStr) {
        if (locStr == null || !locStr.contains(",")) return null;
        try {
            String[] parts = locStr.split(",");
            return new LatLng(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
        } catch (Exception e) {
            Log.e("ParseLatLng", "Failed to parse: " + locStr, e);
            return null;
        }
    }

    private String formatLatLng(LatLng latLng) {
        if (latLng == null) return "";
        return latLng.latitude + "," + latLng.longitude;
    }

    private String getAddressFromLatLng(LatLng latLng, Geocoder geocoder) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) return addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
            Log.e("Geocoder", "Failed to fetch address", e);
        }
        return latLng.latitude + ", " + latLng.longitude;
    }
}
