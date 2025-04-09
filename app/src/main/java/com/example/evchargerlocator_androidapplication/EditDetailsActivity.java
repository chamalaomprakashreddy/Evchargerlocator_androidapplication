package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;
import java.util.Map;

public class EditDetailsActivity extends AppCompatActivity {

    private EditText edtStationName, edtPowerOutput, edtAvailability,edtPricing;
    private Spinner spinnerChargingLevel, spinnerConnectorType, spinnerNetwork;
    private Button btnUpdate;

    private DatabaseReference databaseReference;
    private String stationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        // Set up back arrow
        TextView backArrowText = findViewById(R.id.backArrowText);
        backArrowText.setOnClickListener(v -> finish());

        edtStationName = findViewById(R.id.edtStationName);
        edtPowerOutput = findViewById(R.id.edtPowerOutput);
        edtAvailability = findViewById(R.id.edtAvailability);
        edtPricing = findViewById(R.id.edtPricing);


        spinnerChargingLevel = findViewById(R.id.spinnerChargingLevel);
        spinnerConnectorType = findViewById(R.id.spinnerConnectorType);
        spinnerNetwork = findViewById(R.id.spinnerNetwork);

        btnUpdate = findViewById(R.id.btnUpdate);


        stationId = getIntent().getStringExtra("stationId");
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations").child(stationId);

        setupSpinners();
        loadStationDetails();

        btnUpdate.setOnClickListener(v -> updateStationDetails());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> chargingLevelAdapter = ArrayAdapter.createFromResource(
                this, R.array.levels_array, android.R.layout.simple_spinner_item);
        chargingLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChargingLevel.setAdapter(chargingLevelAdapter);

        ArrayAdapter<CharSequence> connectorTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.connectors_array, android.R.layout.simple_spinner_item);
        connectorTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerConnectorType.setAdapter(connectorTypeAdapter);

        ArrayAdapter<CharSequence> networkAdapter = ArrayAdapter.createFromResource(
                this, R.array.networks_array, android.R.layout.simple_spinner_item);
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNetwork.setAdapter(networkAdapter);
    }

    private void loadStationDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChargingStation station = snapshot.getValue(ChargingStation.class);
                if (station != null) {
                    edtStationName.setText(station.getName());
                    edtPowerOutput.setText(station.getPowerOutput());
                    edtAvailability.setText(station.getAvailability());
                    edtPricing.setText(String.valueOf(station.getPricing())); // Set pricing EditText

                    setSpinnerSelection(spinnerChargingLevel, station.getChargingLevel());
                    setSpinnerSelection(spinnerConnectorType, station.getConnectorType());
                    setSpinnerSelection(spinnerNetwork, station.getNetwork());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditDetailsActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            spinner.setSelection(position);
        }
    }

    private void updateStationDetails() {
        String name = edtStationName.getText().toString().trim();
        String powerOutput = edtPowerOutput.getText().toString().trim();
        String availability = edtAvailability.getText().toString().trim();
        String chargingLevel = spinnerChargingLevel.getSelectedItem().toString();
        String connectorType = spinnerConnectorType.getSelectedItem().toString();
        String network = spinnerNetwork.getSelectedItem().toString();
        String pricingStr = edtPricing.getText().toString().trim();

        if (pricingStr.isEmpty()) {
            Toast.makeText(this, "Please enter pricing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use double instead of int for pricing
        double pricing = Double.parseDouble(pricingStr);

        // Create a map of values to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("powerOutput", powerOutput);
        updates.put("availability", availability);
        updates.put("chargingLevel", chargingLevel);
        updates.put("connectorType", connectorType);
        updates.put("network", network);
        updates.put("pricing", pricing);  // Store as double

        // Update all values at once
        databaseReference.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditDetailsActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditDetailsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }





}
