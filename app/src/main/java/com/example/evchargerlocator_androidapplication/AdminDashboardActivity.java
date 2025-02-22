package com.example.evchargerlocator_androidapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatabaseReference databaseReference;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private Marker searchMarker;
    private List<ChargingStation> stationList = new ArrayList<>();
    private static final int FINE_PERMISSION_CODE = 1;
    private double selectedLat = 0.0, selectedLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up "Add Station" button
        Button btnAddStation = findViewById(R.id.btnAddStation);
        btnAddStation.setOnClickListener(v -> {
            if (selectedLat == 0 && selectedLng == 0) {
                Toast.makeText(this, "Please pin a location first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(AdminDashboardActivity.this, AddStationActivity.class);
            intent.putExtra("latitude", selectedLat);
            intent.putExtra("longitude", selectedLng);
            startActivity(intent);
        });

        // Set up single search functionality
        EditText searchBar = findViewById(R.id.searchBar);
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString();
            if (!query.isEmpty()) {
                searchPlaceOrStation(query);
            } else {
                Toast.makeText(this, "Please enter a location or station to search", Toast.LENGTH_SHORT).show();
            }
        });

        startLocationUpdates();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        }

        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setZoomGesturesEnabled(true);

        // Load stations from Firebase
        loadStationsFromFirebase();

        // Set the map click listener
        myMap.setOnMapClickListener(latLng -> {
            // Clear any existing markers before adding the new one
            myMap.clear();

            // Add a new marker for the selected location
            myMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

            // Zoom the camera to the selected location
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

            // Store the selected latitude and longitude for further use
            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000) // Update every 5 seconds
                .setFastestInterval(2000) // Fastest update interval
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateUserLocation(location);
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateUserLocation(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Remove old marker
        if (userMarker != null) {
            userMarker.remove();
        }

        // Add new marker for user's current location
        userMarker = myMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14));
    }

    private void loadStationsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myMap.clear();
                stationList.clear();
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        stationList.add(station);
                        LatLng location = new LatLng(station.getLatitude(), station.getLongitude());
                        myMap.addMarker(new MarkerOptions().position(location).title(station.getName()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load stations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPlaceOrStation(String query) {
        Geocoder geocoder = new Geocoder(AdminDashboardActivity.this);
        try {
            // First, try to find a place
            List<Address> addressList = geocoder.getFromLocationName(query, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

                // Clear any previous search marker before adding a new one
                if (searchMarker != null) {
                    searchMarker.remove();
                }

                // Add a marker for the search result
                searchMarker = myMap.addMarker(new MarkerOptions().position(location).title(query));
            } else {
                // If no place was found, search for charging stations
                searchStations(query);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(AdminDashboardActivity.this, "Error in searching place or station", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchStations(String query) {
        // Clear any previous station markers
        myMap.clear();

        // Add matching stations
        boolean stationFound = false;
        for (ChargingStation station : stationList) {
            if (station.getName().toLowerCase().contains(query.toLowerCase())) {
                addStationMarker(station); // Add marker for matching stations
                if (!stationFound) {
                    // Zoom in on the first matching station
                    LatLng location = new LatLng(station.getLatitude(), station.getLongitude());
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
                    stationFound = true;
                }
            }
        }

        // If no stations were found, show a message
        if (!stationFound) {
            Toast.makeText(AdminDashboardActivity.this, "No matching station found", Toast.LENGTH_SHORT).show();
        }
    }

    private void addStationMarker(ChargingStation station) {
        LatLng location = new LatLng(station.getLatitude(), station.getLongitude());
        myMap.addMarker(new MarkerOptions().position(location).title(station.getName()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}