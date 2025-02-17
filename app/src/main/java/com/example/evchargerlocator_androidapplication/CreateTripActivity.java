package com.example.evchargerlocator_androidapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


public class CreateTripActivity extends AppCompatActivity {

    private EditText tripName, tripDate, startPoint, endPoint;
    private Button saveTripButton;
    private SharedPreferences sharedPreferences;
    private TextView backArrowText;

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
        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen

        sharedPreferences = getSharedPreferences("MyTrips", Context.MODE_PRIVATE);

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
        String name = tripName.getText().toString().trim();
        String date = tripDate.getText().toString().trim();
        String start = startPoint.getText().toString().trim();
        String end = endPoint.getText().toString().trim();

        if (name.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyTrips", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the trip details as a unique string
        Set<String> tripsSet = new HashSet<>(sharedPreferences.getStringSet("trips", new HashSet<>()));
        tripsSet.add(name + " - " + date + " - " + start + " - " + end);  // Save complete trip details
        editor.putStringSet("trips", tripsSet); // Save updated set
        editor.apply();

        Toast.makeText(this, "Trip saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }


}
