package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.List;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomePageActivity";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs";

    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private DatabaseReference databaseReference;
    private DrawerLayout drawerLayout;
    private ImageView navIcon;
    private NavigationView navigationView;
    private SearchView mapSearchView;
    private FloatingActionButton fabCenter;
    private LatLng startLocation, endLocation;
    private double distanceFilter = 5.0;
    private Polyline routePolyline;
    private Marker searchMarker;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        drawerLayout = findViewById(R.id.drawer_layout);
        navIcon = findViewById(R.id.nav_icon);
        navigationView = findViewById(R.id.nav_view);
        mapSearchView = findViewById(R.id.mapSearch);
        fabCenter = findViewById(R.id.fab_center);

        navIcon.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        setupDrawer();

        Intent intent = getIntent();
        if (intent != null) {
            startLocation = getLatLngFromIntent(intent.getStringExtra("startLocation"));
            endLocation = getLatLngFromIntent(intent.getStringExtra("endLocation"));
            distanceFilter = intent.getDoubleExtra("distanceFilter", 5.0);
        }

        setupSearch();
        fabCenter.setOnClickListener(v -> fetchCurrentLocation());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
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
        } else {
            fetchCurrentLocation();
        }

    }

    private void setupSearch() {
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlace(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchPlace(String query) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(query, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng searchedLocation = new LatLng(address.getLatitude(), address.getLongitude());

                if (searchMarker != null) {
                    searchMarker.remove();
                }

                searchMarker = myMap.addMarker(new MarkerOptions()
                        .position(searchedLocation)
                        .title(query)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 14));
                fetchEVStationsNearby(searchedLocation);

                Log.d(TAG, "Search successful: " + query);
            } else {
                Toast.makeText(this, "No matching place found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error searching place: " + e.getMessage());
        }
    }

    private void fetchEVStationsNearby(LatLng searchedLocation) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());

                        double distance = distanceBetween(searchedLocation, stationLocation);
                        if (distance <= distanceFilter * 1.60934) {
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

    private double distanceBetween(LatLng latLng1, LatLng latLng2) {
        double latDiff = latLng1.latitude - latLng2.latitude;
        double lonDiff = latLng1.longitude - latLng2.longitude;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111.32;
    }

    private void setupDrawer() {
        View headerView = navigationView.getHeaderView(0);

        // ✅ Trip Planner Navigation
        TextView tripPlanner = headerView.findViewById(R.id.menu_trip_planner);
        tripPlanner.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, TripPlannerActivity.class)));

        // ✅ Chat Navigation
        TextView chatOption = headerView.findViewById(R.id.menu_chat);
        chatOption.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, ChatActivity.class)));

        // ✅ Filter Navigation
        TextView filterOption = headerView.findViewById(R.id.menu_filter);
        filterOption.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, FilterActivity.class)));

        // ✅ User Profile Navigation
        TextView userProfile = headerView.findViewById(R.id.menu_user_profile);
        userProfile.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, UserProfileActivity.class)));

        Button logoutButton = headerView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // Log out the user
            FirebaseAuth.getInstance().signOut();  // Sign out from Firebase Auth
            startActivity(new Intent(HomePageActivity.this, MainActivity.class)); // Redirect to login screen
            finish();  // Close this activity
        });

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

                            List<LatLng> routePoints = PolyUtil.decode(polyline);
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
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());

                        // ✅ Check if the EV station is within the specified distance from the route
                        if (PolyUtil.isLocationOnPath(stationLocation, routePoints, true, distanceFilter * 1609.34)) {
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
    private void requestNewLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) return;

                LatLng userLatLng = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14));

                myMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("Updated Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }



    private void fetchCurrentLocation() {
        // ✅ Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14));

                        // ✅ Reverse Geocoding to get the address
                        Geocoder geocoder = new Geocoder(this);
                        List<Address> addressList;
                        try {
                            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addressList != null && !addressList.isEmpty()) {
                                Address address = addressList.get(0);
                                String addressText = address.getAddressLine(0); // Get full address

                                myMap.addMarker(new MarkerOptions()
                                        .position(userLatLng)
                                        .title("Current Location")
                                        .snippet(addressText)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error fetching address: " + e.getMessage());
                        }

                    } else {
                        Log.e(TAG, "Location is null. Requesting new location update.");
                        requestNewLocation(); // If location is null, request a new location update
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get current location: " + e.getMessage());
                    Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
}