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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MyTripsActivity extends AppCompatActivity {

    private TextView backArrowText;
    private ListView tripsListView;
    private ArrayList<String> tripsList;
    private ArrayAdapter<String> tripsAdapter;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;


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

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Trips");

        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen

        loadTrips();

    }


    private void loadTrips() {
        String userId = auth.getCurrentUser().getUid();
        tripsList = new ArrayList<>();

        databaseRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tripsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Trip trip = snapshot.getValue(Trip.class);
                    if (trip != null) {
                        // Display trip with Start & End Point
                        String tripDetails = "📍 " + trip.getStartPoint() + " ➝ " + trip.getEndPoint() + "\n🗓 " + trip.getDate() + " | " + trip.getName();
                        tripsList.add(tripDetails);
                    }
                }
                tripsAdapter = new ArrayAdapter<>(MyTripsActivity.this, android.R.layout.simple_list_item_1, tripsList);
                tripsListView.setAdapter(tripsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyTripsActivity.this, "Failed to load trips!", Toast.LENGTH_SHORT).show();
            }
        });



    }
}