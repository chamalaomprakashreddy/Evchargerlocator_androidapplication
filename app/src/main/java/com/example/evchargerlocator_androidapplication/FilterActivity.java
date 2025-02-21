package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FilterActivity extends AppCompatActivity {

    private Spinner spinnerLevels, spinnerConnectors, spinnerNetworks;
    private TextView textPricing;
    private Button resetButton, applyButton;
    private boolean fromCreateTrip;  // Check if called from CreateTripActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Initialize UI Elements
        TextView backArrowText = findViewById(R.id.backArrowText);
        spinnerLevels = findViewById(R.id.spinner_levels);
        spinnerConnectors = findViewById(R.id.spinner_connectors);
        spinnerNetworks = findViewById(R.id.spinner_networks);
        textPricing = findViewById(R.id.text_pricing);
        resetButton = findViewById(R.id.resetButton);
        applyButton = findViewById(R.id.button_apply);

        // Check if opened from CreateTripActivity
        fromCreateTrip = getIntent().getBooleanExtra("fromCreateTrip", false);

        // Back Arrow Functionality
        backArrowText.setOnClickListener(v -> finish());

        // Reset Filters
        resetButton.setOnClickListener(v -> resetFilters());

        // Apply Filters
        applyButton.setOnClickListener(v -> {
            applyFilters();
        });

        // Listen for spinner selections to update pricing
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                calculatePricing();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerLevels.setOnItemSelectedListener(spinnerListener);
        spinnerConnectors.setOnItemSelectedListener(spinnerListener);
        spinnerNetworks.setOnItemSelectedListener(spinnerListener);
    }

    // Reset Filters
    private void resetFilters() {
        spinnerLevels.setSelection(0);
        spinnerConnectors.setSelection(0);
        spinnerNetworks.setSelection(0);
        textPricing.setText("Select filters to calculate price");
    }

    // Apply Filters Based on Context
    private void applyFilters() {
        String level = spinnerLevels.getSelectedItem().toString();
        String connector = spinnerConnectors.getSelectedItem().toString();
        String network = spinnerNetworks.getSelectedItem().toString();

        if (fromCreateTrip) {
            // Return filters to CreateTripActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("level", level);
            resultIntent.putExtra("connector", connector);
            resultIntent.putExtra("network", network);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // Navigate directly to HomePageActivity with applied filters
            Intent intent = new Intent(FilterActivity.this, HomePageActivity.class);
            intent.putExtra("level", level);
            intent.putExtra("connector", connector);
            intent.putExtra("network", network);
            startActivity(intent);
            finish();
        }
    }

    // Calculate Pricing Based on Selected Filters
    private void calculatePricing() {
        String level = spinnerLevels.getSelectedItem().toString();
        String connector = spinnerConnectors.getSelectedItem().toString();
        String network = spinnerNetworks.getSelectedItem().toString();

        double basePricePerKWh = 0.255;
        double finalPrice = basePricePerKWh;

        if (level.equals("Level 2")) {
            finalPrice *= 1.2;
        } else if (level.equals("DC Fast")) {
            finalPrice *= 1.5;
        }

        if (connector.equals("CHAdeMO")) {
            finalPrice *= 1.1;
        } else if (connector.equals("CCS")) {
            finalPrice *= 1.3;
        }

        if (network.equals("Tesla Supercharger")) {
            finalPrice *= 1.4;
        } else if (network.equals("Electrify America")) {
            finalPrice *= 1.2;
        }

        textPricing.setText(String.format("Estimated Cost: $%.2f per kWh", finalPrice));
    }
}
