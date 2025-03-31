package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
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
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase and Authentication
        firebaseAuth = FirebaseAuth.getInstance();
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

        Button btnViewStations = findViewById(R.id.btnViewStations);
        btnViewStations.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, StationListActivity.class);
            startActivity(intent);
        });

        // Set up search functionality
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

        // Setup More Options Menu (Three Dots for Logout)
        ImageView moreOptions = findViewById(R.id.moreOptions);
        moreOptions.setOnClickListener(view -> showPopupMenu(view));

        startLocationUpdates();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
            getCurrentLocation();  // Fetch current location on map load
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        }

        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setZoomGesturesEnabled(true);

        // Load stations from Firebase
        loadStationsFromFirebase();

        // Set the map click listener to place a marker
        myMap.setOnMapClickListener(latLng -> {
            // Remove only the previous marker, don't clear the whole map
            if (searchMarker != null) {
                searchMarker.remove();
            }

            // Add new marker for the selected location
            searchMarker = myMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

            // Zoom to the selected location
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

            // Store the selected latitude and longitude
            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(), locationCallback, null);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);  // Update every 5 seconds
        locationRequest.setFastestInterval(2000); // Fastest update interval
        // Set priority to high accuracy (not deprecated anymore)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000) // Update every 5 seconds
                .setFastestInterval(2000) // Fastest update interval
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // Updated priority method

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
                stationList.clear();
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        stationList.add(station);
                        LatLng location = new LatLng(station.getLatitude(), station.getLongitude());

                        // Set marker color based on charging level
                        float markerColor;
                        switch (station.getChargingLevel().toLowerCase()) {
                            case "level 1":
                                markerColor = BitmapDescriptorFactory.HUE_YELLOW; // Yellow for Level 1
                                break;
                            case "level 2":
                                markerColor = BitmapDescriptorFactory.HUE_GREEN;  // Green for Level 2
                                break;
                            case "dc fast":
                                markerColor = BitmapDescriptorFactory.HUE_BLUE;   // Blue for DC Fast (changed from red)
                                break;
                            default:
                                markerColor = BitmapDescriptorFactory.HUE_BLUE;   // Default color
                        }

                        myMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(station.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load charging stations", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchPlaceOrStation(String query) {
        Geocoder geocoder = new Geocoder(AdminDashboardActivity.this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(query, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

                if (searchMarker != null) {
                    searchMarker.remove();
                }

                searchMarker = myMap.addMarker(new MarkerOptions().position(location).title(query));
            } else {
                Toast.makeText(this, "Location not found!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error while searching location", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_admin_dashboard, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                logoutUser();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AdminDashboardActivity.this, MainActivity.class));
        finish();
    }

}
