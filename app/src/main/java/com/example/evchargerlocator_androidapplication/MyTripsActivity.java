package com.example.evchargerlocator_androidapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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

public class MyTripsActivity extends AppCompatActivity {

    private TextView backArrowText, emptyView;
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
        emptyView = findViewById(R.id.emptyView);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Trips");

        // Go back to the previous screen
        backArrowText.setOnClickListener(v -> finish());

        // Load trips from Firebase
        loadTrips();
    }

    private void loadTrips() {
        String userId = auth.getCurrentUser().getUid();
        tripsList = new ArrayList<>();

        databaseRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tripsList.clear();

                // Loop through each trip snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Trip trip = snapshot.getValue(Trip.class);
                    if (trip != null) {
                        String tripDetails = "📍 " + trip.getStartPoint() + " ➝ " + trip.getEndPoint() + "\n🗓 " + trip.getDate() + " | " + trip.getName();
                        tripsList.add(tripDetails);
                    }
                }

                tripsAdapter = new ArrayAdapter<>(MyTripsActivity.this, android.R.layout.simple_list_item_1, tripsList);
                tripsListView.setAdapter(tripsAdapter);

                // Handle empty view visibility
                if (tripsList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    tripsListView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    tripsListView.setVisibility(View.VISIBLE);
                }

                // Redirect to HomePage with trip details
                tripsListView.setOnItemClickListener((parent, view, position, id) -> {
                    DataSnapshot selectedSnapshot = getSnapshotAtPosition(dataSnapshot, position);
                    Trip selectedTrip = selectedSnapshot.getValue(Trip.class);

                    if (selectedTrip != null) {
                        Intent intent = new Intent(MyTripsActivity.this, HomePageActivity.class);
                        intent.putExtra("startPoint", selectedTrip.getStartPoint());
                        intent.putExtra("endPoint", selectedTrip.getEndPoint());
                        startActivity(intent);
                    }
                });

                // Delete trip on long press
                tripsListView.setOnItemLongClickListener((parent, view, position, id) -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyTripsActivity.this);
                    builder.setTitle("Delete Trip")
                            .setMessage("Are you sure you want to delete this trip?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                DataSnapshot selectedSnapshot = getSnapshotAtPosition(dataSnapshot, position);
                                selectedSnapshot.getRef().removeValue();
                                Toast.makeText(MyTripsActivity.this, "Trip deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyTripsActivity.this, "Failed to load trips!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to get the snapshot at a specific position
    private DataSnapshot getSnapshotAtPosition(DataSnapshot dataSnapshot, int position) {
        int count = 0;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            if (count == position) {
                return snapshot;
            }
            count++;
        }
        return null;
    }
}
