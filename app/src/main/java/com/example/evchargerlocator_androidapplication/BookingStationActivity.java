package com.example.evchargerlocator_androidapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class BookingStationActivity extends AppCompatActivity {

    private TextView stationNameTextView, stationLocationTextView;
    private EditText dateEditText, timeEditText, vehicleModelEditText, licensePlateEditText;
    private Button paymentNowButton;

    private String stationId, stationName, stationLocation;

    private FirebaseAuth mAuth;
    private DatabaseReference bookingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_station);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");

        // Retrieve Station Details from Intent
        stationId = getIntent().getStringExtra("stationId");
        stationName = getIntent().getStringExtra("stationName");
        stationLocation = getIntent().getStringExtra("stationLocation");

        // Initialize UI Elements
        stationNameTextView = findViewById(R.id.stationNameTextView);
        stationLocationTextView = findViewById(R.id.stationLocationTextView);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        vehicleModelEditText = findViewById(R.id.vehicleModelEditText);
        licensePlateEditText = findViewById(R.id.licensePlateEditText);
        paymentNowButton = findViewById(R.id.paymentNowButton);

        // Set Charging Station Details
        stationNameTextView.setText(stationName);
        stationLocationTextView.setText(stationLocation);

        // Date Picker
        dateEditText.setOnClickListener(v -> showDatePicker());

        // Time Picker
        timeEditText.setOnClickListener(v -> showTimePicker());

        // Payment Now Button
        paymentNowButton.setOnClickListener(v -> proceedToPayment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                        dateEditText.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear),
                year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) ->
                        timeEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)),
                hour, minute, true);
        timePickerDialog.show();
    }

    private void proceedToPayment() {
        String date = dateEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();
        String vehicleModel = vehicleModelEditText.getText().toString().trim();
        String licensePlate = licensePlateEditText.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(vehicleModel) || TextUtils.isEmpty(licensePlate)) {
            Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String bookingId = bookingsRef.push().getKey();

        HashMap<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("bookingId", bookingId);
        bookingDetails.put("userId", userId);
        bookingDetails.put("stationId", stationId);
        bookingDetails.put("stationName", stationName);
        bookingDetails.put("stationLocation", stationLocation);
        bookingDetails.put("date", date);
        bookingDetails.put("time", time);
        bookingDetails.put("vehicleModel", vehicleModel);
        bookingDetails.put("licensePlate", licensePlate);
        bookingDetails.put("status", "Payment Pending");

        assert bookingId != null;
        bookingsRef.child(userId).child(bookingId).setValue(bookingDetails)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookingStationActivity.this, "Proceeding to Payment!", Toast.LENGTH_SHORT).show();
                    // Navigate to PaymentActivity
                    Intent intent = new Intent(BookingStationActivity.this, PaymentActivity.class);
                    intent.putExtra("bookingId", bookingId);
                    intent.putExtra("stationName", stationName);
                    intent.putExtra("stationLocation", stationLocation);
                    intent.putExtra("date", date);
                    intent.putExtra("time", time);
                    intent.putExtra("vehicleModel", vehicleModel);
                    intent.putExtra("licensePlate", licensePlate);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(BookingStationActivity.this, "Failed to Proceed. Try again!", Toast.LENGTH_SHORT).show());
    }
}
