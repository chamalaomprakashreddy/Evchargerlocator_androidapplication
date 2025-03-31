package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
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
import java.util.ArrayList;
import java.util.List;

public class HomePageActivity2 extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomePageActivity";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

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
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page2);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), GOOGLE_MAPS_API_KEY);
        }
        placesClient = Places.createClient(this);

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

        setupSearchWithAutocomplete();
        fabCenter.setOnClickListener(v -> fetchCurrentLocation());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fetchAllEVStations();
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

    private void fetchAllEVStations() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myMap.clear();

                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());

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
                                markerColor = BitmapDescriptorFactory.HUE_BLUE;    // Red for DC Fast
                                break;
                            default:
                                markerColor = BitmapDescriptorFactory.HUE_BLUE;   // Default color
                        }

                        myMap.addMarker(new MarkerOptions()
                                .position(stationLocation)
                                .title(station.getName())
                                .snippet("EV Charging Station")
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity2.this, "Error fetching EV stations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchWithAutocomplete() {
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlace(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    fetchPlaceSuggestions(newText);
                }
                return false;
            }
        });

        mapSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = mapSearchView.getSuggestionsAdapter().getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String suggestion = cursor.getString(cursor.getColumnIndex("suggestion"));
                    mapSearchView.setQuery(suggestion, true);
                    return true;
                }
                return false;
            }
        });
    }

    private void fetchPlaceSuggestions(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("US")
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<String> suggestions = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                suggestions.add(prediction.getPrimaryText(null).toString());
            }

            MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "suggestion"});
            for (int i = 0; i < suggestions.size(); i++) {
                cursor.addRow(new Object[]{i, suggestions.get(i)});
            }

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    cursor,
                    new String[]{"suggestion"},
                    new int[]{android.R.id.text1},
                    0
            );

            mapSearchView.setSuggestionsAdapter(adapter);
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error fetching place suggestions: " + exception.getMessage());
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
                Toast.makeText(HomePageActivity2.this, "Error fetching EV stations", Toast.LENGTH_SHORT).show();
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

        TextView tripPlanner = headerView.findViewById(R.id.menu_trip_planner);
        tripPlanner.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, TripPlannerActivity.class)));

        TextView chatOption = headerView.findViewById(R.id.menu_chat);
        chatOption.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, ChatActivity.class)));

        TextView filterOption = headerView.findViewById(R.id.menu_filter);
        filterOption.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, FilterActivity.class)));

        TextView userProfile = headerView.findViewById(R.id.menu_user_profile);
        userProfile.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, UserProfileActivity.class)));

        Button logoutButton = headerView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomePageActivity2.this, MainActivity.class));
            finish();
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
                Toast.makeText(HomePageActivity2.this, "Error fetching EV stations", Toast.LENGTH_SHORT).show();
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14));

                        Geocoder geocoder = new Geocoder(this);
                        List<Address> addressList;
                        try {
                            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addressList != null && !addressList.isEmpty()) {
                                Address address = addressList.get(0);
                                String addressText = address.getAddressLine(0);

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
                        requestNewLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get current location: " + e.getMessage());
                    Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}