package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatabaseReference databaseReference;
    private BottomNavigationView bottomNavigationView;
    private List<ChargingStation> stationList = new ArrayList<>(); // Store all stations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mapSearchView = findViewById(R.id.mapSearch);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        setupBottomNavigation();
        setupSearch();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        }

        // Enable Zoom Controls inside the Map UI
        myMap.getUiSettings().setZoomControlsEnabled(true); // Enables zoom in/out buttons inside the map
        myMap.getUiSettings().setZoomGesturesEnabled(true); // Enables pinch zooming

        loadStationsFromFirebase(); // Load charging stations
    }

    private void loadStationsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myMap.clear(); // Clear existing markers
                stationList.clear(); // Clear old station data

                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        stationList.add(station); // Store stations for searching
                        addStationMarker(station); // Add marker to map
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity.this, "Failed to load stations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addStationMarker(ChargingStation station) {
        LatLng location = new LatLng(station.getLatitude(), station.getLongitude());

        // Customize marker color based on availability
        float markerColor = station.getAvailability().equalsIgnoreCase("Available") ?
                BitmapDescriptorFactory.HUE_GREEN :
                BitmapDescriptorFactory.HUE_RED;

        Marker marker = myMap.addMarker(new MarkerOptions()
                .position(location)
                .title(station.getName())
                .snippet("Power: " + station.getPowerOutput() + "\nStatus: " + station.getAvailability())
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

        if (marker != null) {
            marker.showInfoWindow(); // Show station details
        }
    }

    private void setupSearch() {
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterStations(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                filterStations(query);
                return false;
            }
        });
    }

    private void filterStations(String query) {
        myMap.clear(); // Clear old markers

        for (ChargingStation station : stationList) {
            if (station.getName().toLowerCase().contains(query.toLowerCase())) {
                addStationMarker(station); // Show only matching stations
            }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (myMap != null) {
                    myMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
                }
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if  (itemId == R.id.activity_trip_planner) {
                startActivity(new Intent(this, TripPlannerActivity.class));
            } else if (itemId == R.id.activity_chat) {
                startActivity(new Intent(this, ChatActivity.class));
            } else if (itemId == R.id.activity_filter) {
                startActivity(new Intent(this, FilterActivity.class));
            } else if (itemId == R.id.activity_user_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
            }
            return true;
        });
    }
}
