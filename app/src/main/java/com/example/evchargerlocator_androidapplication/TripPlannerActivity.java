package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen

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

        submitButton.setOnClickListener(v -> {
            String start = startPoint.getText().toString();
            String end = endPoint.getText().toString();

            if (end.isEmpty()) {
                Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use user's location if start point is empty
            if (start.isEmpty()) {
                start = userLatitude + "," + userLongitude;
            }

            // Pass locations to HomePageActivity for direction display
            Intent intent = new Intent(TripPlannerActivity.this, HomePageActivity.class);
            intent.putExtra("startLocation", start.isEmpty() ? userLatitude + "," + userLongitude : start);
            intent.putExtra("endLocation", end);
            startActivity(intent);
        });
    }


}