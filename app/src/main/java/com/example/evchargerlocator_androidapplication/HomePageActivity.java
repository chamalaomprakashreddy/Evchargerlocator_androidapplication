package com.example.evchargerlocator_androidapplication;
import static android.util.Log.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageManager;
import android.Manifest;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;
    private DatabaseReference databaseReference;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView navIcon;
    private List<ChargingStation> stationList = new ArrayList<>(); // Store all stations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize Drawer and Navigation Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navIcon = findViewById(R.id.nav_icon);

        // Set up nav icon to open the navigation drawer
        navIcon.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));

        // Initialize the SearchView
        mapSearchView = findViewById(R.id.mapSearch); // Ensure this ID matches the one in XML
        if (mapSearchView != null) {
            setupSearch();
        } else {
            // Handle the case where mapSearchView is not found
            e("HomePageActivity", "SearchView not found.");
        }

        // Initialize Firebase and map
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up drawer menu click handling
        setupDrawer();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        }

        // Enable zoom controls
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setZoomGesturesEnabled(true);

        // Load charging stations from Firebase
        loadStationsFromFirebase();
    }

    private void loadStationsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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
            public void onCancelled(DatabaseError error) {
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

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.activity_trip_planner) {
                startActivity(new Intent(this, TripPlannerActivity.class));
            } else if (itemId == R.id.activity_chat) {
                startActivity(new Intent(this, ChatActivity.class));
            } else if (itemId == R.id.activity_filter) {
                startActivity(new Intent(this, FilterActivity.class));
            } else if (itemId == R.id.activity_user_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
            }
            drawerLayout.closeDrawer(Gravity.LEFT); // Close drawer after selection
            return true;
        });
    }
}