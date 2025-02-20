package com.example.evchargerlocator_androidapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


public class CreateTripActivity extends AppCompatActivity {

    private EditText tripName, tripDate, startPoint, endPoint;
    private Button saveTripButton;
    private TextView backArrowText;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth; // Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_trip);

        tripName = findViewById(R.id.tripName);
        tripDate = findViewById(R.id.tripDate);
        startPoint = findViewById(R.id.startPoint);  // Starting point
        endPoint = findViewById(R.id.endPoint);  // Ending point
        saveTripButton = findViewById(R.id.saveTripButton);
        backArrowText = findViewById(R.id.backArrowText);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Trips");

        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen

        // Date Picker
        tripDate.setOnClickListener(v -> showDatePicker());

        // Save Trip Button Click
        saveTripButton.setOnClickListener(v -> saveTrip());
    }
        private void showDatePicker () {
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
        Trip trip = new Trip(tripId, name, date, start, end);


        // Store under the user's ID
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
