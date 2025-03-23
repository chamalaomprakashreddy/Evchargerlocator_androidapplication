package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StationAdapter stationAdapter;
    private List<ChargingStation> stationList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_list);

        // Set up back arrow
        TextView backArrowText = findViewById(R.id.back_arrow);
        backArrowText.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewStations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        stationList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        String currentAdminId = firebaseAuth.getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        loadStationsFromFirebase(currentAdminId);
    }

    private void loadStationsFromFirebase(String adminId) {
        databaseReference.orderByChild("adminId").equalTo(adminId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stationList.clear();
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    ChargingStation station = stationSnapshot.getValue(ChargingStation.class);
                    if (station != null) {
                        stationList.add(station);
                    }
                }
                stationAdapter = new StationAdapter(stationList, StationListActivity.this);
                recyclerView.setAdapter(stationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StationListActivity.this, "Failed to load stations", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
