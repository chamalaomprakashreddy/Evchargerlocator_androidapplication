package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TripPlannerActivity extends AppCompatActivity {

    private TextView backArrowText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_planner);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backArrowText = findViewById(R.id.backArrowText);
        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen

        Button createTripButton = findViewById(R.id.createTripButton);
        createTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripPlannerActivity.this, CreateTripActivity.class);
                startActivity(intent);
            }
        });

        Button myTripsButton = findViewById(R.id.myTripsButton);
        myTripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripPlannerActivity.this, MyTripsActivity.class);
                startActivity(intent);
            }
        });


    }
}