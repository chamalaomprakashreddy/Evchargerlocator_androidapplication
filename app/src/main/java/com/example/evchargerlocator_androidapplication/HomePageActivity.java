package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomePageActivity";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs";
    private static final int FILTER_REQUEST_CODE = 1002;

    private GoogleMap myMap;
    private RequestQueue requestQueue;
    private DatabaseReference databaseReference;
    private LatLng startLocation, endLocation;
    private double distanceFilter = 5.0;
    private Polyline routePolyline;
    private String selectedLevel;
    private String selectedConnector;
    private String selectedNetwork;
    private List<LatLng> routePoints; // Store route points for re-filtering

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        requestQueue = Volley.newRequestQueue(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        Intent intent = getIntent();
        if (intent != null) {
            startLocation = getLatLngFromIntent(intent.getStringExtra("startLocation"));
            endLocation = getLatLngFromIntent(intent.getStringExtra("endLocation"));
            distanceFilter = intent.getDoubleExtra("distanceFilter", 5.0);
            selectedLevel = intent.getStringExtra("selectedLevel");
            selectedConnector = intent.getStringExtra("selectedConnector");
            selectedNetwork = intent.getStringExtra("selectedNetwork");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ImageButton filterButton = findViewById(R.id.filter);
        filterButton.setOnClickListener(view -> {
            Intent filterIntent = new Intent(HomePageActivity.this, FilterActivity.class);
            filterIntent.putExtra("selectedLevel", selectedLevel);
            filterIntent.putExtra("selectedConnector", selectedConnector);
            filterIntent.putExtra("selectedNetwork", selectedNetwork);
            startActivityForResult(filterIntent, FILTER_REQUEST_CODE); // Use startActivityForResult
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        if (startLocation != null && endLocation != null) {
            drawRouteAndEVStations();
        }
    }

    private void drawRouteAndEVStations() {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + startLocation.latitude + "," + startLocation.longitude
                + "&destination=" + endLocation.latitude + "," + endLocation.longitude
                + "&key=" + GOOGLE_MAPS_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            String polyline = route.getJSONObject("overview_polyline").getString("points");

                            routePoints = PolyUtil.decode(polyline); // Store route points
                            if (routePolyline != null) routePolyline.remove();

                            routePolyline = myMap.addPolyline(new PolylineOptions()
                                    .addAll(routePoints)
                                    .width(10)
                                    .color(ContextCompat.getColor(this, R.color.black)));

                            fetchEVStationsAlongRoute(routePoints);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing route response: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Route request failed: " + error.getMessage()));

        requestQueue.add(request);
    }

    private void fetchEVStationsAlongRoute(List<LatLng> routePoints) {
        myMap.clear(); // Clear existing markers and polyline
        if (routePolyline != null) {
            routePolyline.remove();
        }
        routePolyline = myMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .width(10)
                .color(ContextCompat.getColor(this, R.color.black)));

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());

                        boolean isWithinRoute = PolyUtil.isLocationOnPath(stationLocation, routePoints, true, distanceFilter * 1609.34);

                        boolean matchesFilters = true;
                        if (selectedLevel != null && !selectedLevel.isEmpty() && !station.getChargingLevel().equals(selectedLevel)) {
                            matchesFilters = false;
                        }
                        if (selectedConnector != null && !selectedConnector.isEmpty() && !station.getConnectorType().equals(selectedConnector)) {
                            matchesFilters = false;
                        }
                        if (selectedNetwork != null && !selectedNetwork.isEmpty() && !station.getNetwork().equals(selectedNetwork)) {
                            matchesFilters = false;
                        }

                        if (isWithinRoute && matchesFilters) {
                            myMap.addMarker(new MarkerOptions()
                                    .position(stationLocation)
                                    .title(station.getName())
                                    .snippet("EV Charging Station")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity.this, "Error fetching EV stations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private LatLng getLatLngFromIntent(String locationString) {
        if (locationString == null || locationString.isEmpty()) return null;
        try {
            String[] latLng = locationString.split(",");
            return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing LatLng from Intent: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLevel = data.getStringExtra("selectedLevel");
            selectedConnector = data.getStringExtra("selectedConnector");
            selectedNetwork = data.getStringExtra("selectedNetwork");

            // Re-fetch and filter stations with new filters
            if (routePoints != null) {
                fetchEVStationsAlongRoute(routePoints);
            }
        }
    }
}