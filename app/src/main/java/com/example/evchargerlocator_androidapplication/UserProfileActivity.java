package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    private EditText fullName, phoneNumber, email, vehicle;
    private Button editButton, paymentButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private boolean isEditing = false; // Track if in edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Views
        TextView backArrowText = findViewById(R.id.backArrowText);
        fullName = findViewById(R.id.fullName);
        phoneNumber = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.email);
        vehicle = findViewById(R.id.vehicle);
        editButton = findViewById(R.id.editButton);
        paymentButton = findViewById(R.id.PaymentButton);

        // Navigate to Profile when clicking back arrow
        backArrowText.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            showUserData();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        editButton.setOnClickListener(v -> toggleEditProfile());
        paymentButton.setOnClickListener(v -> openPaymentActivity());
    }

    /**
     * Fetches user data from Firebase and displays it in the UI.
     */
    public void showUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fullName.setText(snapshot.child("fullName").getValue(String.class));
                    email.setText(snapshot.child("email").getValue(String.class)); // Email remains unchanged
                    phoneNumber.setText(snapshot.child("phoneNumber").getValue(String.class));
                    vehicle.setText(snapshot.child("vehicle").getValue(String.class));
                }
                setFieldsEnabled(false); // Disable fields after fetching data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Toggles between Edit and Save mode.
     */
    private void toggleEditProfile() {
        isEditing = !isEditing;

        if (isEditing) {
            // Enable fields for editing
            setFieldsEnabled(true);
            editButton.setText("Save");
        } else {
            // Save the updated data
            saveUserProfile();
            setFieldsEnabled(false);
            editButton.setText("Edit Profile");
        }
    }

    /**
     * Completely replaces old user data in Firebase with new values.
     */
    private void saveUserProfile() {
        String updatedFullName = fullName.getText().toString();
        String updatedPhoneNumber = phoneNumber.getText().toString();
        String updatedVehicle = vehicle.getText().toString();

        if (updatedFullName.isEmpty() || updatedPhoneNumber.isEmpty() || updatedVehicle.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new User object with the updated data
        User updatedUser = new User(email.getText().toString(), updatedFullName, updatedPhoneNumber, updatedVehicle);

        // Overwrite the user data in Firebase
        userRef.setValue(updatedUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UserProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Enables or disables the input fields.
     */
    private void setFieldsEnabled(boolean enabled) {
        fullName.setEnabled(enabled);
        phoneNumber.setEnabled(enabled);
        vehicle.setEnabled(enabled);

        if (enabled) {
            fullName.setFocusableInTouchMode(true);
            phoneNumber.setFocusableInTouchMode(true);
            vehicle.setFocusableInTouchMode(true);
        } else {
            fullName.setFocusable(false);
            phoneNumber.setFocusable(false);
            vehicle.setFocusable(false);
        }
    }

    /**
     * Opens the Payment activity.
     */
    private void openPaymentActivity() {
        startActivity(new Intent(UserProfileActivity.this, PaymentActivity.class));
    }
}