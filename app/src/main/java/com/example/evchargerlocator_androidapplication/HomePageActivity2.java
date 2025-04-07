package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomePageActivity2 extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomePageActivity";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int FILTER_REQUEST_CODE = 1002;

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
    private double distanceFilter = 5.0; // Distance filter in miles
    private Polyline routePolyline;
    private Marker searchMarker;
    private PlacesClient placesClient;
    private ImageView filterButton;

    // Filter variables
    private boolean filterLevel1 = false;
    private boolean filterLevel2 = false;
    private boolean filterLevel3 = false;
    private Set<String> filterConnectorTypes = new HashSet<>();
    private boolean filterNetworkTesla = false;
    private boolean filterNetworkChargePoint = false;
    private boolean filterNetworkEVgo = false;
    private boolean filterNetworkElectrify = false;

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
        filterButton = findViewById(R.id.filterbutton);

        navIcon.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        filterButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity2.this, FilterActivity.class);
            startActivityForResult(intent, FILTER_REQUEST_CODE);
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

        myMap.setOnMarkerClickListener(marker -> {
            if (marker.getSnippet() != null && marker.getSnippet().equals("EV Charging Station")) {
                showBottomSheet(marker);
                return true;
            }
            return false;
        });

        if (startLocation != null && endLocation != null) {
            drawRouteAndEVStations();
        } else {
            fetchCurrentLocation();
        }
    }

    private void setupDrawer() {
        View headerView = navigationView.getHeaderView(0);

        TextView tripPlanner = headerView.findViewById(R.id.menu_trip_planner);
        tripPlanner.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, TripPlannerActivity.class)));

        TextView chatOption = headerView.findViewById(R.id.menu_chat);
        chatOption.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, ChatActivity.class)));

        TextView userProfile = headerView.findViewById(R.id.menu_user_profile);
        userProfile.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, UserProfileActivity.class)));

        TextView faqs = headerView.findViewById(R.id.faqs);
        faqs.setOnClickListener(v -> startActivity(new Intent(HomePageActivity2.this, Faqs.class)));


        Button logoutButton = headerView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomePageActivity2.this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            filterLevel1 = data.getBooleanExtra("level1", false);
            filterLevel2 = data.getBooleanExtra("level2", false);
            filterLevel3 = data.getBooleanExtra("level3", false);

            String[] connectorTypes = data.getStringArrayExtra("connectorTypes");
            filterConnectorTypes.clear();
            if (connectorTypes != null) {
                filterConnectorTypes.addAll(Arrays.asList(connectorTypes));
            }

            filterNetworkTesla = data.getBooleanExtra("networkTesla", false);
            filterNetworkChargePoint = data.getBooleanExtra("networkChargePoint", false);
            filterNetworkEVgo = data.getBooleanExtra("networkEVgo", false);
            filterNetworkElectrify = data.getBooleanExtra("networkElectrify", false);

            fetchAllEVStations();
            if (startLocation != null && endLocation != null) {
                drawRouteAndEVStations();
            } else if (searchMarker != null) {
                fetchEVStationsNearby(searchMarker.getPosition());
            }
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAllEVStations() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myMap.clear();

                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null && passesFilter(station)) {
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());
                        float markerColor = getMarkerColor(station.getChargingLevel());

                        Marker marker = myMap.addMarker(new MarkerOptions()
                                .position(stationLocation)
                                .title(station.getName())
                                .snippet("EV Charging Station")
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                        marker.setTag(station);
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
                    @SuppressLint("Range") String suggestion = cursor.getString(cursor.getColumnIndex("suggestion"));
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
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

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
                    if (station != null && passesFilter(station)) {
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());
                        double distance = distanceBetween(searchedLocation, stationLocation);
                        if (distance <= distanceFilter) {
                            Marker marker = myMap.addMarker(new MarkerOptions()
                                    .position(stationLocation)
                                    .title(station.getName())
                                    .snippet("EV Charging Station")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            marker.setTag(station);
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
        double distanceInKm = Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111.32;
        return distanceInKm * 0.621371; // Convert kilometers to miles
    }

    private void drawRouteAndEVStations() {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + startLocation.latitude + "," + startLocation.longitude
                + "&destination=" + endLocation.latitude + "," + endLocation.longitude
                + "&units=imperial"
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
                    if (station != null && passesFilter(station)) {
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());
                        if (PolyUtil.isLocationOnPath(stationLocation, routePoints, true, distanceFilter * 1609.34)) {
                            Marker marker = myMap.addMarker(new MarkerOptions()
                                    .position(stationLocation)
                                    .title(station.getName())
                                    .snippet("EV Charging Station")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            marker.setTag(station);
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

    private boolean passesFilter(ChargingStation station) {
        boolean noLevelFilter = !filterLevel1 && !filterLevel2 && !filterLevel3;
        boolean noConnectorFilter = filterConnectorTypes.isEmpty();
        boolean noNetworkFilter = !filterNetworkTesla && !filterNetworkChargePoint && !filterNetworkEVgo && !filterNetworkElectrify;

        if (noLevelFilter && noConnectorFilter && noNetworkFilter) {
            return true;
        }

        String level = station.getChargingLevel().toLowerCase();
        boolean levelMatch = (filterLevel1 && level.contains("level 1")) ||
                (filterLevel2 && level.contains("level 2")) ||
                (filterLevel3 && level.contains("dc fast"));

        String connectorType = station.getConnectorType() != null ? station.getConnectorType() : "";
        boolean connectorMatch = filterConnectorTypes.isEmpty() || filterConnectorTypes.contains(connectorType);

        String network = station.getNetwork() != null ? station.getNetwork().toLowerCase() : "";
        boolean networkMatch = (filterNetworkTesla && network.contains("tesla")) ||
                (filterNetworkChargePoint && network.contains("chargepoint")) ||
                (filterNetworkEVgo && network.contains("evgo")) ||
                (filterNetworkElectrify && network.contains("electrify america"));

        if (!noLevelFilter && !levelMatch) return false;
        if (!noConnectorFilter && !connectorMatch) return false;
        if (!noNetworkFilter && !networkMatch) return false;

        return true;
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

    private void requestNewLocation(LocationCallbackHandler callback) {
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
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                fusedLocationClient.removeLocationUpdates(this);
                callback.onLocationReceived(userLatLng);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    interface LocationCallbackHandler {
        void onLocationReceived(LatLng latLng);
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
                        requestNewLocation(latLng -> {});
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get current location: " + e.getMessage());
                    Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private float getMarkerColor(String chargingLevel) {
        switch (chargingLevel.toLowerCase()) {
            case "level 1":
                return BitmapDescriptorFactory.HUE_YELLOW;
            case "level 2":
                return BitmapDescriptorFactory.HUE_GREEN;
            case "dc fast":
                return BitmapDescriptorFactory.HUE_BLUE;
            default:
                return BitmapDescriptorFactory.HUE_BLUE;
        }
    }

    private String calculatePricing(ChargingStation station) {
        String level = station.getChargingLevel() != null ? station.getChargingLevel().toLowerCase() : "";
        String connector = station.getConnectorType() != null ? station.getConnectorType().toLowerCase() : "";
        String network = station.getNetwork() != null ? station.getNetwork().toLowerCase() : "";

        double basePricePerKWh = 0.0;

        // Base pricing based on charging level
        if (level.contains("level 1")) {
            basePricePerKWh = 0.20; // Cheaper for Level 1
        } else if (level.contains("level 2")) {
            basePricePerKWh = 0.35; // Moderate for Level 2
        } else if (level.contains("dc fast")) {
            basePricePerKWh = 0.50; // Expensive for DC Fast
        } else {
            basePricePerKWh = 0.40; // Default fallback
        }

        // Adjust price based on connector type
        if (connector.contains("tesla")) {
            basePricePerKWh += 0.10; // Premium for Tesla connectors
        } else if (connector.contains("ccs") || connector.contains("chademo")) {
            basePricePerKWh += 0.05; // Slight increase for fast-charging connectors
        }

        // Adjust price based on network
        if (network.contains("tesla")) {
            basePricePerKWh += 0.15; // Tesla network premium
        } else if (network.contains("electrify america")) {
            basePricePerKWh += 0.10; // Higher rates for Electrify America
        } else if (network.contains("chargepoint")) {
            basePricePerKWh -= 0.05; // Slightly cheaper for ChargePoint
        } else if (network.contains("evgo")) {
            basePricePerKWh += 0.05; // Moderate increase for EVgo
        }

        // Format the price as a string
        return String.format("$%.2f/kWh", basePricePerKWh);
    }

    private void showBottomSheet(Marker marker) {
        ChargingStation station = (ChargingStation) marker.getTag();
        if (station == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_station, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView stationName = bottomSheetView.findViewById(R.id.station_name);
        TextView stationAddress = bottomSheetView.findViewById(R.id.station_address);
        TextView stationLevel = bottomSheetView.findViewById(R.id.station_level);
        TextView stationPricing = bottomSheetView.findViewById(R.id.station_pricing);
        Button navigateButton = bottomSheetView.findViewById(R.id.navigate_button);
        Button detailsButton = bottomSheetView.findViewById(R.id.details_button);

        stationName.setText(station.getName());
        stationLevel.setText("Charging Level: " + station.getChargingLevel());

        // ✅ Fetch price from Firebase object directly
        double price = station.getPricing();
        String pricing = String.format("$%.2f/kWh", price);
        stationPricing.setText("Pricing: " + pricing);

        // 🧭 Fetch address from coordinates
        String addressText = "Address not available";
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocation(station.getLatitude(), station.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                addressText = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
        }
        stationAddress.setText(addressText);
        final String finalAddressText = addressText;

        navigateButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("google.navigation:q=" + station.getLatitude() + "," + station.getLongitude()));
            startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        detailsButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        LatLng stationLocation = new LatLng(station.getLatitude(), station.getLongitude());

                        double distanceMiles = distanceBetween(userLocation, stationLocation);
                        String distanceStr = String.format("%.2f miles", distanceMiles);

                        fetchTravelTime(userLocation, stationLocation, (distanceResult, durationResult) -> {
                            Intent intent = new Intent(HomePageActivity2.this, StationDetailsActivity.class);
                            intent.putExtra("stationName", station.getName());
                            intent.putExtra("latitude", station.getLatitude());
                            intent.putExtra("longitude", station.getLongitude());
                            intent.putExtra("address", finalAddressText);
                            intent.putExtra("distance", distanceResult != null ? distanceResult : distanceStr);
                            intent.putExtra("duration", durationResult != null ? durationResult : "N/A");
                            intent.putExtra("plugType", station.getChargingLevel());
                            intent.putExtra("plugPrice", pricing);
                            intent.putExtra("plugAvailability", station.getAvailability());
                            startActivity(intent);
                            bottomSheetDialog.dismiss();
                        });
                    } else {
                        requestNewLocation(latLng -> {
                            double distanceMiles = distanceBetween(latLng, new LatLng(station.getLatitude(), station.getLongitude()));
                            String distanceStr = String.format("%.2f miles", distanceMiles);
                            fetchTravelTime(latLng, new LatLng(station.getLatitude(), station.getLongitude()), (distanceResult, durationResult) -> {
                                Intent intent = new Intent(HomePageActivity2.this, StationDetailsActivity.class);
                                intent.putExtra("stationName", station.getName());
                                intent.putExtra("latitude", station.getLatitude());
                                intent.putExtra("longitude", station.getLongitude());
                                intent.putExtra("address", finalAddressText);
                                intent.putExtra("distance", distanceResult != null ? distanceResult : distanceStr);
                                intent.putExtra("duration", durationResult != null ? durationResult : "N/A");
                                intent.putExtra("plugType", station.getChargingLevel());
                                intent.putExtra("plugPrice", pricing);
                                intent.putExtra("plugAvailability", station.getAvailability());
                                startActivity(intent);
                                bottomSheetDialog.dismiss();
                            });
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }


    private void fetchTravelTime(LatLng origin, LatLng destination, TravelTimeCallback callback) {
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?"
                + "origins=" + origin.latitude + "," + origin.longitude
                + "&destinations=" + destination.latitude + "," + destination.longitude
                + "&units=imperial"
                + "&key=" + GOOGLE_MAPS_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray rows = response.getJSONArray("rows");
                        if (rows.length() > 0) {
                            JSONObject elements = rows.getJSONObject(0).getJSONArray("elements").getJSONObject(0);
                            String distance = elements.getJSONObject("distance").getString("text");
                            String duration = elements.getJSONObject("duration").getString("text");
                            callback.onResult(distance, duration);
                        } else {
                            callback.onResult(null, null);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing Distance Matrix response: " + e.getMessage());
                        callback.onResult(null, null);
                    }
                },
                error -> {
                    Log.e(TAG, "Distance Matrix request failed: " + error.getMessage());
                    callback.onResult(null, null);
                });

        requestQueue.add(request);
    }

    interface TravelTimeCallback {
        void onResult(String distance, String duration);
    }
}