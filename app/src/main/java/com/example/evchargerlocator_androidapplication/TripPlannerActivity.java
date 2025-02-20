package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class TripPlannerActivity extends AppCompatActivity {

    private TextView backArrowText;
    private EditText startPoint, endPoint;
    private Button submitButton;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double userLatitude, userLongitude;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_planner);

        backArrowText = findViewById(R.id.backArrowText);
        startPoint = findViewById(R.id.startPoint);
        endPoint = findViewById(R.id.endPoint);
        submitButton = findViewById(R.id.submitButton);
        //Initialize location Services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen


        // Intent for Select Filters Button to redirect to FilterActivity
        Button selectFiltersButton = findViewById(R.id.selectFiltersButton);
        selectFiltersButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripPlannerActivity.this, FilterActivity.class);
            startActivity(intent);  // Redirect to FilterActivity
        });

        //Intent for CreateTripActivity
        Button createTripButton = findViewById(R.id.createTripButton);
        createTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripPlannerActivity.this, CreateTripActivity.class);
                startActivity(intent);
            }
        });

        //Intent for MyTripsActivity
        Button myTripsButton = findViewById(R.id.myTripsButton);
        myTripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripPlannerActivity.this, MyTripsActivity.class);
                startActivity(intent);
            }
        });
        //Handle trip submission
        submitButton.setOnClickListener(v -> {
            String start = startPoint.getText().toString();
            String end = endPoint.getText().toString();

            if (end.isEmpty()) {
                Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch user location if start point is empty
            if (start.isEmpty()) {
                getUserLocationAndProceed(end);
            } else {
                navigateToHomePage(start, end);
            }
        });
        // Request location permission
        checkLocationPermission();
    }

    private void getUserLocationAndProceed(String endLocation) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                    String userLocation = userLatitude + "," + userLongitude;
                    navigateToHomePage(userLocation, endLocation);
                } else {
                    Toast.makeText(this, "Could not get current location. Please enter start point.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void navigateToHomePage(String startLocation, String endLocation) {
        Intent intent = new Intent(TripPlannerActivity.this, HomePageActivity.class);
        intent.putExtra("startLocation", startLocation);
        intent.putExtra("endLocation", endLocation);
        startActivity(intent);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied! Please enter start point manually.", Toast.LENGTH_LONG).show();
            }

        }


    }
}