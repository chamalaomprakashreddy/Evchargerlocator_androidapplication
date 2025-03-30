package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        adapter = new SavedTripAdapter(tripList, trip -> {
            Intent intent = new Intent(getContext(), MyTripActivity.class);
            intent.putExtra("fromAddress", trip.getFromAddress());
            intent.putExtra("toAddress", trip.getToAddress());
            intent.putExtra("startLocation", trip.getFromLatLng());
            intent.putExtra("endLocation", trip.getToLatLng());
            intent.putParcelableArrayListExtra("stations", new ArrayList<>(trip.getStations()));
            startActivity(intent);
        });

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
}
