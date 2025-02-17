package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MyTripsActivity extends AppCompatActivity {

    private TextView backArrowText;
    private ListView tripsListView;
    private ArrayList<String> tripsList;
    private ArrayAdapter<String> tripsAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_trips);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backArrowText = findViewById(R.id.backArrowText);
        tripsListView = findViewById(R.id.tripsListView);


        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen
        sharedPreferences = getSharedPreferences("MyTrips", Context.MODE_PRIVATE);

        // Load saved trips from SharedPreferences
        loadTrips();
        // Set item click listener to open map
        tripsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTrip = tripsList.get(position);
            openMap(selectedTrip);
        });
    }

    private void loadTrips() {
        Set<String> tripsSet = sharedPreferences.getStringSet("trips", new HashSet<>());
        tripsList = new ArrayList<>(tripsSet);

        if (tripsList.isEmpty()) {
            Toast.makeText(this, "No trips found. Create a new trip!", Toast.LENGTH_SHORT).show();
        }

        tripsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tripsList);
        tripsListView.setAdapter(tripsAdapter);
    }

    private void openMap(String tripDetails) {
        String[] details = tripDetails.split(" - ");
        if (details.length == 4) {
            String tripName = details[0];
            String tripDate = details[1];
            String startPoint = details[2];
            String endPoint = details[3];

            // For now, use placeholder coordinates for the start and end points
            double startLat = 37.7749;  // Example: Replace with actual location for your trip
            double startLng = -122.4194;
            double endLat = 37.7849;    // Example: Replace with actual location for your trip
            double endLng = -122.4294;

            Intent intent = new Intent(MyTripsActivity.this, HomePageActivity.class);
            intent.putExtra("tripName", tripName);
            intent.putExtra("tripDate", tripDate);
            intent.putExtra("startPoint", startPoint);
            intent.putExtra("endPoint", endPoint);
            intent.putExtra("startLat", startLat);
            intent.putExtra("startLng", startLng);
            intent.putExtra("endLat", endLat);
            intent.putExtra("endLng", endLng);
            startActivity(intent);
        }
    }
}