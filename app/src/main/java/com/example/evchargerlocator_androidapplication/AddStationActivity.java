package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddStationActivity extends AppCompatActivity {

    private double latitude, longitude;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_station);

        // Set up back arrow
        TextView backArrowText = findViewById(R.id.backArrowText);
        backArrowText.setOnClickListener(v -> finish());

        firebaseAuth = FirebaseAuth.getInstance();

        // Navigate to Profile when clicking back arrow


        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);

        TextView txtLocation = findViewById(R.id.txtLocation);
        txtLocation.setText("Lat: " + latitude + ", Lng: " + longitude);

        EditText edtStationName = findViewById(R.id.edtStationName);
        EditText edtPowerOutput = findViewById(R.id.edtPowerOutput);
        EditText edtAvailability = findViewById(R.id.edtAvailability);

        Spinner spinnerChargingLevel = findViewById(R.id.spinnerChargingLevel);
        Spinner spinnerConnectorType = findViewById(R.id.spinnerConnectorType);
        Spinner spinnerNetwork = findViewById(R.id.spinnerNetwork);

        Button btnSaveStation = findViewById(R.id.btnSaveStation);

        // Populate Spinners with predefined options
        ArrayAdapter<CharSequence> chargingLevelAdapter = ArrayAdapter.createFromResource(
                this, R.array.charging_levels, android.R.layout.simple_spinner_item);
        chargingLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChargingLevel.setAdapter(chargingLevelAdapter);

        ArrayAdapter<CharSequence> connectorTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.connector_types, android.R.layout.simple_spinner_item);
        connectorTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerConnectorType.setAdapter(connectorTypeAdapter);

        ArrayAdapter<CharSequence> networkAdapter = ArrayAdapter.createFromResource(
                this, R.array.networks, android.R.layout.simple_spinner_item);
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNetwork.setAdapter(networkAdapter);

        btnSaveStation.setOnClickListener(v -> {
            String name = edtStationName.getText().toString().trim();
            String powerOutput = edtPowerOutput.getText().toString().trim();
            String availability = edtAvailability.getText().toString().trim();
            String chargingLevel = spinnerChargingLevel.getSelectedItem().toString();
            String connectorType = spinnerConnectorType.getSelectedItem().toString();
            String network = spinnerNetwork.getSelectedItem().toString();

            if (name.isEmpty() || powerOutput.isEmpty() || availability.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String adminId = firebaseAuth.getCurrentUser().getUid();
            String stationId = databaseReference.push().getKey();

            ChargingStation station = new ChargingStation(stationId, name, latitude, longitude, powerOutput, availability, chargingLevel, connectorType, network, adminId);

            if (stationId != null) {
                databaseReference.child(stationId).setValue(station).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Station added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add station", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
