package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class CreateTripFragment extends Fragment {

    private TextInputEditText startPoint, endPoint;
    private SeekBar distanceSeekBar;
    private TextView distanceText;
    private Button findRouteButton;
    private LatLng startLatLng, endLatLng;

    private static final String TAG = "CreateTripFragment";
    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs"; // ✅ Using API Key

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_trip, container, false);

        startPoint = view.findViewById(R.id.startPoint);
        endPoint = view.findViewById(R.id.endPoint);
        distanceSeekBar = view.findViewById(R.id.distanceSeekBar);
        distanceText = view.findViewById(R.id.distanceText);
        findRouteButton = view.findViewById(R.id.submitButton);

        // ✅ Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), GOOGLE_PLACES_API_KEY);
        }

        setupAutocomplete(startPoint, true);
        setupAutocomplete(endPoint, false);

        // ✅ SeekBar Listener
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

        // ✅ "Find Route" Button Click
        findRouteButton.setOnClickListener(v -> {
            if (startLatLng == null || endLatLng == null) {
                Toast.makeText(requireContext(), "Please select valid locations.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(requireContext(), HomePageActivity.class);
            intent.putExtra("startLocation", startLatLng.latitude + "," + startLatLng.longitude);
            intent.putExtra("endLocation", endLatLng.latitude + "," + endLatLng.longitude);
            intent.putExtra("distanceFilter", distanceSeekBar.getProgress());
            startActivity(intent);
        });

        return view;
    }

    private void setupAutocomplete(TextInputEditText textView, boolean isStart) {
        textView.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("US")
                    .build(requireContext());

            if (isStart) startAutocompleteLauncher.launch(intent);
            else endAutocompleteLauncher.launch(intent);
        });
    }

    // ✅ Handles Autocomplete results
    private final ActivityResultLauncher<Intent> startAutocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AutocompleteActivity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    startLatLng = place.getLatLng();
                    startPoint.setText(place.getName());
                    Log.d(TAG, "Start Location Selected: " + place.getName());
                }
            });

    private final ActivityResultLauncher<Intent> endAutocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AutocompleteActivity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    endLatLng = place.getLatLng();
                    endPoint.setText(place.getName());
                    Log.d(TAG, "End Location Selected: " + place.getName());
                }
            });
}
