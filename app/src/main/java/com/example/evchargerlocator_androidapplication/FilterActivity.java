package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FilterActivity extends AppCompatActivity {

    private Spinner spinnerLevels, spinnerConnectors, spinnerNetworks;
    private TextView textPricing;
    private Button resetButton, applyButton;

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

        // Back Arrow Functionality
        backArrowText.setOnClickListener(v -> {
            Intent intent = new Intent(FilterActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });

        // Reset Filters
        resetButton.setOnClickListener(v -> resetFilters());

        // Apply Filters & Calculate Pricing
        applyButton.setOnClickListener(v -> {
            calculatePricing();
            Intent intent = new Intent(FilterActivity.this, HomePageActivity.class);
            startActivity(intent);
        });

        // Listen for spinner selections to update pricing
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculatePricing();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
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

    // Calculate Pricing Based on Selected Filters
    private void calculatePricing() {
        String level = spinnerLevels.getSelectedItem().toString();
        String connector = spinnerConnectors.getSelectedItem().toString();
        String network = spinnerNetworks.getSelectedItem().toString();

        double basePricePerKWh = 0.255;  // Default Missouri price per kWh
        double finalPrice = basePricePerKWh;

        // Adjust price based on selected filters
        if (level.equals("Level 2")) {
            finalPrice *= 1.2; // Increase by 20% for Level 2 chargers
        } else if (level.equals("DC Fast")) {
            finalPrice *= 1.5; // Increase by 50% for DC Fast chargers
        }

        if (connector.equals("CHAdeMO")) {
            finalPrice *= 1.1; // Increase by 10% for CHAdeMO connectors
        } else if (connector.equals("CCS")) {
            finalPrice *= 1.3; // Increase by 30% for CCS connectors
        }

        if (network.equals("Tesla Supercharger")) {
            finalPrice *= 1.4; // Increase by 40% for Tesla Superchargers
        } else if (network.equals("Electrify America")) {
            finalPrice *= 1.2; // Increase by 20% for Electrify America
        }

        textPricing.setText(String.format("Estimated Cost: $%.2f per kWh", finalPrice));
    }
}
