package com.example.evchargerlocator_androidapplication;

import static android.util.Log.e;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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
    private DatabaseReference databaseReference;
    private DrawerLayout drawerLayout;
    private ImageView navIcon;
    private NavigationView navigationView;  // Declare NavigationView
    private List<ChargingStation> stationList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng userLocation;
    private MarkerOptions userLocationMarker; // Marker for user's location
    private Marker searchMarker;  // Marker for search result

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize Drawer and Navigation Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navIcon = findViewById(R.id.nav_icon);
        navigationView = findViewById(R.id.nav_view);  // Initialize NavigationView

        // Set up nav icon to open the navigation drawer
        navIcon.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));

        // Initialize the SearchView
        mapSearchView = findViewById(R.id.mapSearch);
        if (mapSearchView != null) {
            setupSearch();
        } else {
            e("HomePageActivity", "SearchView not found.");
        }

        // Initialize Firebase and map
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize FusedLocationProviderClient for live location tracking
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup LocationRequest for continuous updates
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Setup LocationCallback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLocations() != null) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            updateMapWithLocation(location); // Update live location marker
                        }
                    }
                }
            }
        };

        // Floating action button (FAB) for recentering the map to the live location
        FloatingActionButton fabCenter = findViewById(R.id.fab_center);
        fabCenter.setOnClickListener(v -> {
            if (userLocation != null) {
                // Recenter the map to the most recent live location and keep the charging stations visible
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });

        // Set up Drawer Navigation
        setupDrawer();
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

    private void updateMapWithLocation(Location location) {
        // Update the map with the user's current location (optional)
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        }

        // Start location updates
        getCurrentLocation();

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

                // Add charging stations markers first (they should be on top of the live location marker)
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        stationList.add(station); // Store stations for searching
                        addStationMarker(station); // Add marker to map
                    }
                }

                // Add live location marker after the charging station markers (so it appears below them)
                if (userLocation != null) {
                    addUserLocationMarker(userLocation);
                }

                // Ensure that the map zoom level is appropriate
                if (userLocation != null) {
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15)); // Adjust zoom level if needed
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
        myMap.addMarker(new MarkerOptions()
                .position(location)
                .title(station.getName())
                .snippet("Power: " + station.getPowerOutput() + "\nStatus: " + station.getAvailability()));
    }

    private void addUserLocationMarker(LatLng userLocation) {
        // Remove the old marker if it exists
        if (userLocationMarker != null) {
            myMap.clear(); // Remove previous user location marker
        }

        // Add a new marker for the user's current location
        userLocationMarker = new MarkerOptions()
                .position(userLocation)
                .title("Your Location");
        myMap.addMarker(userLocationMarker);
    }

    private void setupSearch() {
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlace(query);
                searchStations(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchMarker != null) {
                    searchMarker.remove();  // Clear the previous search marker
                }
                return false;
            }
        });
    }

    private void searchPlace(String query) {
        Geocoder geocoder = new Geocoder(HomePageActivity.this);
        try {
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
                Toast.makeText(HomePageActivity.this, "it is ev station", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HomePageActivity.this, "Error in searching place", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(HomePageActivity.this, "it is a place", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(); // Retry getting location if permission is granted
            } else {
                Toast.makeText(this, "Permission denied. Unable to get location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
