package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;



public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private BottomNavigationView bottomNavigationView;
    private TextView distanceText, durationText;
    private String startLocation, endLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mapSearchView = findViewById(R.id.mapSearch);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Intent intent = getIntent();
        startLocation = intent.getStringExtra("startLocation");
        endLocation = intent.getStringExtra("endLocation");

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        fetchDirections();
        setupSearch();
        setupBottomNavigation();
    }

    private void setupSearch() {
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().isEmpty()) {
                    Toast.makeText(HomePageActivity.this, "Enter a valid location", Toast.LENGTH_SHORT).show();
                    return false;
                }

                Geocoder geocoder = new Geocoder(HomePageActivity.this, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocationName(query, 1);
                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        if (myMap != null) {
                            myMap.clear();
                            myMap.addMarker(new MarkerOptions().position(latLng).title(query));
                            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        }
                    } else {
                        Toast.makeText(HomePageActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(HomePageActivity.this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if(itemId==R.id.activity_home_page){
                startActivity(new Intent(this, HomePageActivity.class));
            } else if  (itemId == R.id.activity_trip_planner) {
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

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (myMap != null) {
                    myMap.clear();
                    myMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
                }
            }
        });
    }

    private void fetchDirections() {
        if (startLocation == null || endLocation == null) return;
        new Thread(() -> {
            try {
                String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + startLocation + "&destination=" + endLocation + "&key=AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

                    String distance = leg.getJSONObject("distance").getString("text");
                    String duration = leg.getJSONObject("duration").getString("text");

                    runOnUiThread(() -> {
                        distanceText.setText("Distance: " + distance);
                        durationText.setText("Duration: " + duration);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        }

        if (startLocation != null && endLocation != null) {
            String[] startCoords = startLocation.split(",");
            String[] endCoords = endLocation.split(",");

            LatLng startLatLng = new LatLng(Double.parseDouble(startCoords[0]), Double.parseDouble(startCoords[1]));
            LatLng endLatLng = new LatLng(Double.parseDouble(endCoords[0]), Double.parseDouble(endCoords[1]));

            myMap.addMarker(new MarkerOptions().position(startLatLng).title("Start Location"));
            myMap.addMarker(new MarkerOptions().position(endLatLng).title("Destination"));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 10));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(this, "Location permission denied. Please enable it in settings.", Toast.LENGTH_SHORT).show();
        }
    }
}
