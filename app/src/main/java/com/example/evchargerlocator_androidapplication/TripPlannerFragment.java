package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import java.util.Arrays;
import java.util.List;

public class TripPlannerFragment extends Fragment {

    private AutoCompleteTextView startPoint, endPoint;
    private SeekBar distanceSeekBar;
    private TextView distanceText;
    private Button findRouteButton;
    private LatLng startLatLng, endLatLng;
    private PlacesClient placesClient;
    private static final String TAG = "TripPlannerFragment";
    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs"; // Replace with your actual API Key

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_planner, container, false); // Ensure this XML exists in res/layout/

        // ✅ Initialize UI Elements
        startPoint = view.findViewById(R.id.startPoint);
        endPoint = view.findViewById(R.id.endPoint);
        distanceSeekBar = view.findViewById(R.id.distanceSeekBar);
        distanceText = view.findViewById(R.id.distanceText);
        findRouteButton = view.findViewById(R.id.submitButton);

        // ✅ Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), GOOGLE_PLACES_API_KEY);
        }
        placesClient = Places.createClient(requireContext());

        // ✅ Setup Autocomplete for Start & End Points
        setupAutocomplete(startPoint, true);
        setupAutocomplete(endPoint, false);

        // ✅ Distance SeekBar Listener
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceText.setText("Show charging stations within " + progress + " mi");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ✅ Find Route Button Click Listener
        findRouteButton.setOnClickListener(v -> {
            if (startLatLng == null || endLatLng == null) {
                Toast.makeText(requireContext(), "Please select valid locations.", Toast.LENGTH_SHORT).show();
                return;
            }

            int distanceFilter = distanceSeekBar.getProgress();

            // ✅ Start HomePageActivity with route data
            Intent intent = new Intent(requireContext(), HomePageActivity.class);
            intent.putExtra("startLocation", startLatLng.latitude + "," + startLatLng.longitude);
            intent.putExtra("endLocation", endLatLng.latitude + "," + endLatLng.longitude);
            intent.putExtra("distanceFilter", distanceFilter);
            startActivity(intent);
        });

        return view;
    }

    /**
     * ✅ Sets up Google Places Autocomplete for a given AutoCompleteTextView
     */
    private void setupAutocomplete(AutoCompleteTextView textView, boolean isStartPoint) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        textView.setAdapter(new PlacesAutoCompleteAdapter(requireContext(), placesClient, fields));

        textView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlace = (String) parent.getItemAtPosition(position);
            textView.setText(selectedPlace);
            fetchLatLng(selectedPlace, isStartPoint);
        });
    }

    /**
     * ✅ Fetches LatLng for a selected place
     */
    private void fetchLatLng(String placeName, boolean isStartPoint) {
        Places.createClient(requireContext())
                .findCurrentPlace(null)
                .addOnSuccessListener(response -> {
                    for (PlaceLikelihood likelihood : response.getPlaceLikelihoods()) {
                        Place place = likelihood.getPlace(); // Extract Place object

                        if (place.getName().equalsIgnoreCase(placeName)) {
                            if (isStartPoint) {
                                startLatLng = place.getLatLng();
                                Log.d(TAG, "Start Location: " + place.getLatLng());
                            } else {
                                endLatLng = place.getLatLng();
                                Log.d(TAG, "End Location: " + place.getLatLng());
                            }
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching LatLng: " + e.getMessage()));
    }

}
