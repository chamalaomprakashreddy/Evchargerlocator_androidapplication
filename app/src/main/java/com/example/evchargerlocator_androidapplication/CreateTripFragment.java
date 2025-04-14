package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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

    private static final String TAG = "CreateTripFragment";
    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs"; // Replace with actual key

    private TextInputEditText startPoint, endPoint;
    private SeekBar distanceSeekBar, batterySeekBar;
    private TextView distanceText, batteryText;
    private Spinner vehicleSpinner;
    private Button findRouteButton;

    private LatLng startLatLng, endLatLng;
    private String selectedVehicle = "Tesla";
    private int batteryPercent = 50;

    private boolean selectingStart = true;

    private final ActivityResultLauncher<Intent> autocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AutocompleteActivity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    if (selectingStart) {
                        startLatLng = place.getLatLng();
                        startPoint.setText(place.getName());
                        Log.d(TAG, "Start: " + place.getName());
                    } else {
                        endLatLng = place.getLatLng();
                        endPoint.setText(place.getName());
                        Log.d(TAG, "End: " + place.getName());
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_trip, container, false);

        startPoint = view.findViewById(R.id.startPoint);
        endPoint = view.findViewById(R.id.endPoint);
        distanceSeekBar = view.findViewById(R.id.distanceSeekBar);
        distanceText = view.findViewById(R.id.distanceText);
        batterySeekBar = view.findViewById(R.id.batterySeekBar);
        batteryText = view.findViewById(R.id.batteryText);
        vehicleSpinner = view.findViewById(R.id.vehicleSpinner);
        findRouteButton = view.findViewById(R.id.submitButton);
       // saveButton = view.findViewById(R.id.saveButton);

        // Initialize Google Places
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), GOOGLE_PLACES_API_KEY);
        }

        setupAutocomplete();

        // Spinner setup
        String[] vehicles = {"Tesla", "Audi", "BMW", "Lamborghini", "Generic"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, vehicles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleSpinner.setAdapter(adapter);

        vehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVehicle = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                selectedVehicle = "Tesla";
            }
        });

        // Battery slider
        batterySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryPercent = progress;
                batteryText.setText("Battery Level: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Distance slider
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceText.setText("Show stations within " + progress + " mi");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Route button
        findRouteButton.setOnClickListener(v -> {
            if (startLatLng == null || endLatLng == null) {
                Toast.makeText(requireContext(), "Please select both locations", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(requireContext(), HomePageActivity.class);
            intent.putExtra("startLocation", formatLatLng(startLatLng));
            intent.putExtra("endLocation", formatLatLng(endLatLng));
            intent.putExtra("distanceFilter", distanceSeekBar.getProgress());
            intent.putExtra("vehicleType", selectedVehicle);
            intent.putExtra("batteryPercent", batteryPercent);
            startActivity(intent);
        });


        return view;
    }

    private void setupAutocomplete() {
        startPoint.setOnClickListener(v -> {
            selectingStart = true;
            launchAutocomplete();
        });

        endPoint.setOnClickListener(v -> {
            selectingStart = false;
            launchAutocomplete();
        });
    }

    private void launchAutocomplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("US")
                .build(requireContext());
        autocompleteLauncher.launch(intent);
    }

    private String formatLatLng(LatLng latLng) {
        return latLng.latitude + "," + latLng.longitude;
    }
}
