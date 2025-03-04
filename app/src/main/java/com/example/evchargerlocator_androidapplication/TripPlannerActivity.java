package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TripPlannerActivity extends AppCompatActivity {

    private EditText startPoint, endPoint; // Changed from 'destination' to 'endPoint'
    private Button createTripButton, myTripsButton, selectFiltersButton, submitButton;
    private TextView backArrowText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        // Apply edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        backArrowText = findViewById(R.id.backArrowText);
        startPoint = findViewById(R.id.startPoint);
        endPoint = findViewById(R.id.endPoint); // ✅ Fixed: Use correct ID from XML
        createTripButton = findViewById(R.id.createTripButton);
        myTripsButton = findViewById(R.id.myTripsButton);
        selectFiltersButton = findViewById(R.id.selectFiltersButton);
        submitButton = findViewById(R.id.submitButton);

        // Back button click event
        backArrowText.setOnClickListener(v -> finish());

        // Create trip button action
        createTripButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripPlannerActivity.this, CreateTripActivity.class);
            startActivity(intent);
        });

        // My Trips button action
        myTripsButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripPlannerActivity.this, MyTripsActivity.class);
            startActivity(intent);
        });

        // Select filters button action
        selectFiltersButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripPlannerActivity.this, FilterActivity.class);
            intent.putExtra("fromTripPlanner", true);
            startActivity(intent);
        });

        // Submit button action
        submitButton.setOnClickListener(v -> {
            String start = startPoint.getText().toString().trim();
            String end = endPoint.getText().toString().trim();

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please enter both Start and End points", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Trip planned successfully!", Toast.LENGTH_SHORT).show();
                // Additional logic for trip planning can be added here
            }
        });
    }
}
