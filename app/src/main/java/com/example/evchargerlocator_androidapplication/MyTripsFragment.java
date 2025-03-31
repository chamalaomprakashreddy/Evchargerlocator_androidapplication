package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyTripsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SavedTripAdapter adapter;
    private ArrayList<SavedTrip> tripList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);

        recyclerView = view.findViewById(R.id.myTripsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tripList = new ArrayList<>();

        // Initialize the adapter and pass the listener for both click and delete actions
        adapter = new SavedTripAdapter(tripList, new SavedTripAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(SavedTrip trip) {
                // Handle trip click (open details, navigate, etc.)
                Intent intent = new Intent(getContext(), MyTripActivity.class);
                intent.putExtra("fromAddress", trip.getFromAddress());
                intent.putExtra("toAddress", trip.getToAddress());
                intent.putExtra("startLocation", trip.getFromLatLng());
                intent.putExtra("endLocation", trip.getToLatLng());
                intent.putParcelableArrayListExtra("stations", new ArrayList<>(trip.getStations()));
                startActivity(intent);
            }

            @Override
            public void onTripDelete(SavedTrip trip, int position) {
                // Handle delete
                deleteTripFromDatabase(trip, position); // Delete trip from database and remove from adapter
            }
        }, getContext()); // Pass context here

        recyclerView.setAdapter(adapter);

        loadSavedTrips();

        return view;
    }

    private void loadSavedTrips() {
        DatabaseReference tripsRef = FirebaseDatabase.getInstance().getReference("SavedTrips");

        tripsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tripList.clear();
                for (DataSnapshot tripSnap : snapshot.getChildren()) {
                    SavedTrip trip = tripSnap.getValue(SavedTrip.class);
                    if (trip != null) {
                        tripList.add(trip);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle Firebase error
            }
        });
    }

    private void deleteTripFromDatabase(SavedTrip trip, int position) {
        // Firebase code to delete the trip from the database
        DatabaseReference tripsRef = FirebaseDatabase.getInstance().getReference("SavedTrips");
        tripsRef.child(trip.getTripName()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Remove from the adapter list
                    adapter.removeItem(position);
                    // Show toast message
                    Toast.makeText(getContext(), "Trip deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Show error toast if deletion fails
                    Toast.makeText(getContext(), "Failed to delete trip", Toast.LENGTH_SHORT).show();
                });
    }
}
