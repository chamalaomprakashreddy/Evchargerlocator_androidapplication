package com.example.evchargerlocator_androidapplication;

import static android.util.Log.e;
import android.Manifest;
import android.content.Context;  // Import Context
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;  // Import LayoutInflater
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
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
        View headerView = navigationView.getHeaderView(0);

        // Initialize menu items
        TextView tripPlanner = headerView.findViewById(R.id.menu_trip_planner);
        TextView chat = headerView.findViewById(R.id.menu_chat);
        TextView filter = headerView.findViewById(R.id.menu_filter);
        TextView userProfile = headerView.findViewById(R.id.menu_user_profile);

        // Set click listeners
        tripPlanner.setOnClickListener(v -> startActivity(new Intent(this, TripPlannerActivity.class)));
        chat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        filter.setOnClickListener(v -> startActivity(new Intent(this, FilterActivity.class)));
        userProfile.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)));

        // Handle Logout
        Button logoutButton = headerView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // Log out the user
            FirebaseAuth.getInstance().signOut();  // Sign out from Firebase Auth
            startActivity(new Intent(HomePageActivity.this, MainActivity.class)); // Redirect to login screen
            finish();  // Close this activity
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

        // Set a marker click listener to show the details popup
        myMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof ChargingStation) {
                ChargingStation station = (ChargingStation) marker.getTag();
                showDetailsPopup(station);
                return true;
            }
            return false;
        });
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
        Marker marker = myMap.addMarker(new MarkerOptions().position(location));
        marker.setTag(station);
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

    private void showDetailsPopup(ChargingStation station) {
        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_details, null);

        // Initialize UI elements
        TextView stationName = popupView.findViewById(R.id.popup_station_name);
        Button detailsButton = popupView.findViewById(R.id.popup_details_button);

        // Set station details
        stationName.setText(station.getName());

        // Handle button click to open station details activity
        detailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, StationDetailsActivity.class);
            intent.putExtra("stationId", station.getStationId());
            intent.putExtra("name", station.getName());
            intent.putExtra("latitude", station.getLatitude());
            intent.putExtra("longitude", station.getLongitude());
            intent.putExtra("powerOutput", station.getPowerOutput());
            intent.putExtra("availability", station.getAvailability());
            intent.putExtra("chargingLevel", station.getChargingLevel());
            intent.putExtra("connectorType", station.getConnectorType());
            intent.putExtra("network", station.getNetwork());
            startActivity(intent);
        });

        // Create and display the popup
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.showAtLocation(findViewById(R.id.map), Gravity.CENTER, 0, 0);
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
                Toast.makeText(HomePageActivity.this, "No matching place found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HomePageActivity.this, "Error in searching place", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchStations(String query) {
        // Clear any previous station markers
        //myMap.clear();

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
            Toast.makeText(HomePageActivity.this, "No stations found", Toast.LENGTH_SHORT).show();
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