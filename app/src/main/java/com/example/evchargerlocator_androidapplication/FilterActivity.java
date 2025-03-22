package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FilterActivity extends AppCompatActivity {

    private Spinner levelsSpinner, connectorsSpinner, networksSpinner;
    private TextView pricingTextView, backArrowText;
    private Button resetButton, applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        levelsSpinner = findViewById(R.id.spinner_levels);
        connectorsSpinner = findViewById(R.id.spinner_connectors);
        networksSpinner = findViewById(R.id.spinner_networks);
        pricingTextView = findViewById(R.id.text_pricing);
        resetButton = findViewById(R.id.resetButton);
        applyButton = findViewById(R.id.button_apply);
        backArrowText = findViewById(R.id.backArrowText);

        // Set the click listener for the back arrow
        backArrowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity
            }
        });

        ArrayAdapter<CharSequence> levelsAdapter = ArrayAdapter.createFromResource(this,
                R.array.levels_array, android.R.layout.simple_spinner_item);
        levelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelsSpinner.setAdapter(levelsAdapter);

        ArrayAdapter<CharSequence> connectorsAdapter = ArrayAdapter.createFromResource(this,
                R.array.connectors_array, android.R.layout.simple_spinner_item);
        connectorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        connectorsSpinner.setAdapter(connectorsAdapter);

        ArrayAdapter<CharSequence> networksAdapter = ArrayAdapter.createFromResource(this,
                R.array.networks_array, android.R.layout.simple_spinner_item);
        networksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networksSpinner.setAdapter(networksAdapter);

        // Pre-select spinner values based on incoming intent
        Intent intent = getIntent();
        if (intent != null) {
            String selectedLevel = intent.getStringExtra("selectedLevel");
            String selectedConnector = intent.getStringExtra("selectedConnector");
            String selectedNetwork = intent.getStringExtra("selectedNetwork");

            if (selectedLevel != null) levelsSpinner.setSelection(((ArrayAdapter<String>) levelsSpinner.getAdapter()).getPosition(selectedLevel));
            if (selectedConnector != null) connectorsSpinner.setSelection(((ArrayAdapter<String>) connectorsSpinner.getAdapter()).getPosition(selectedConnector));
            if (selectedNetwork != null) networksSpinner.setSelection(((ArrayAdapter<String>) networksSpinner.getAdapter()).getPosition(selectedNetwork));
        }

        applyButton.setOnClickListener(v -> {
            String selectedLevel = levelsSpinner.getSelectedItem().toString();
            String selectedConnector = connectorsSpinner.getSelectedItem().toString();
            String selectedNetwork = networksSpinner.getSelectedItem().toString();

            double price = calculatePrice(selectedLevel, selectedConnector, selectedNetwork);
            pricingTextView.setText("" + price);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedLevel", selectedLevel);
            resultIntent.putExtra("selectedConnector", selectedConnector);
            resultIntent.putExtra("selectedNetwork", selectedNetwork);
            setResult(RESULT_OK, resultIntent);
            finish(); // Return to HomePageActivity
        });

        resetButton.setOnClickListener(v -> {
            levelsSpinner.setSelection(0);
            connectorsSpinner.setSelection(0);
            networksSpinner.setSelection(0);
            pricingTextView.setText("Select filters to calculate price");
        });
    }

    private double calculatePrice(String level, String connector, String network) {
        double basePrice = 0;

        switch (level) {
            case "Level 1":
                basePrice += 5.00;
                break;
            case "Level 2":
                basePrice += 10.00;
                break;
            case "DC Fast":
                basePrice += 20.00;
                break;
            default:
                basePrice = 0;
        }

        switch (connector) {
            case "Type 1":
                basePrice += 2.00;
                break;
            case "Type 2":
                basePrice += 3.00;
                break;
            case "CCS":
                basePrice += 5.00;
                break;
            case "CHAdeMO":
                basePrice += 7.00;
                break;
        }

        switch (network) {
            case "Tesla":
                basePrice += 1.00;
                break;
            case "ChargePoint":
                basePrice += 1.50;
                break;
            case "EVgo":
                basePrice += 2.00;
                break;
            case "Electrify America":
                basePrice += 3.00;
                break;
        }

        return basePrice;
    }
}