package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class StationDetailsActivity extends AppCompatActivity {

    private TextView stationNameText, powerOutputText, availabilityText,
            chargingLevelText, connectorTypeText, networkText;
    private Button bookStationButton;

    private String name, powerOutput, availability, chargingLevel, connectorType, network;
    private double latitude, longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        // Set up back arrow
        TextView backArrowText = findViewById(R.id.back_arrow);
        backArrowText.setOnClickListener(v -> finish());

        stationNameText = findViewById(R.id.station_name);
        powerOutputText = findViewById(R.id.power_output_text);
        availabilityText = findViewById(R.id.availability_text);
        chargingLevelText = findViewById(R.id.charging_level_text);
        connectorTypeText = findViewById(R.id.connector_type_text);
        networkText = findViewById(R.id.network_text);
        bookStationButton = findViewById(R.id.btn_book_station);

        // Retrieve data from Intent
        name = getIntent().getStringExtra("name");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        powerOutput = getIntent().getStringExtra("powerOutput");
        availability = getIntent().getStringExtra("availability");
        chargingLevel = getIntent().getStringExtra("chargingLevel");
        connectorType = getIntent().getStringExtra("connectorType");
        network = getIntent().getStringExtra("network");

        if (name != null) {
            stationNameText.setText(name);
            powerOutputText.setText(powerOutput);
            availabilityText.setText(availability);
            chargingLevelText.setText(chargingLevel);
            connectorTypeText.setText(connectorType);
            networkText.setText(network);
        } else {
            Toast.makeText(this, "Error: Station details not available", Toast.LENGTH_LONG).show();
        }

        // Booking button click event
        bookStationButton.setOnClickListener(view -> {
            Intent intent = new Intent(StationDetailsActivity.this, BookingStationActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivity(intent);
        });
    }
}
