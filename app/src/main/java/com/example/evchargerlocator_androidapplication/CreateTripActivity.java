package com.example.evchargerlocator_androidapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class CreateTripActivity extends AppCompatActivity {

    private EditText tripName, tripDate, startPoint, endPoint;
    private Button saveTripButton, selectFiltersButton;
    private TextView backArrowText;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    // Filters data
    private String selectedLevel = "", selectedConnector = "", selectedNetwork = "";
    private static final int FILTER_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Enable Edge-to-Edge UI correctly
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_create_trip);

        tripName = findViewById(R.id.tripName);
        tripDate = findViewById(R.id.tripDate);
        startPoint = findViewById(R.id.startPoint);
        endPoint = findViewById(R.id.endPoint);
        saveTripButton = findViewById(R.id.saveTripButton);
        backArrowText = findViewById(R.id.backArrowText);
        selectFiltersButton = findViewById(R.id.selectFiltersButton);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Trips");

        backArrowText.setOnClickListener(v -> finish());

        tripDate.setOnClickListener(v -> showDatePicker());

        saveTripButton.setOnClickListener(v -> saveTrip());

        selectFiltersButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateTripActivity.this, FilterActivity.class);
            intent.putExtra("fromCreateTrip", true);
            startActivityForResult(intent, FILTER_REQUEST_CODE);
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            tripDate.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLevel = data.getStringExtra("level");
            selectedConnector = data.getStringExtra("connector");
            selectedNetwork = data.getStringExtra("network");
        }
    }

    private void saveTrip() {
        String userId = auth.getCurrentUser().getUid();
        String name = tripName.getText().toString().trim();
        String date = tripDate.getText().toString().trim();
        String start = startPoint.getText().toString().trim();
        String end = endPoint.getText().toString().trim();

        if (name.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        String tripId = databaseRef.child(userId).push().getKey();
        if (tripId == null) {
            Toast.makeText(this, "Error creating trip ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the trip with filters
        Trip trip = new Trip(tripId, name, date, start, end, selectedLevel, selectedConnector, selectedNetwork);

        databaseRef.child(userId).child(tripId).setValue(trip).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Trip saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save trip", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
