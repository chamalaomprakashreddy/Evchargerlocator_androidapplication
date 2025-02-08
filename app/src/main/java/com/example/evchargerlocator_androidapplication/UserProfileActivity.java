package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileActivity extends AppCompatActivity {

    private EditText fullName, phoneNumber, email, vehicle;
    private Button editButton, logoutButton, paymentButton;
    private TextView backArrowText;
    private ImageView profileImage, editProfileImage;
    private FirebaseAuth firebaseAuth;

    private boolean isEditing = false; // Track edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Views
        fullName = findViewById(R.id.fullName);
        phoneNumber = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.email);
        vehicle = findViewById(R.id.vehicle);
        editButton = findViewById(R.id.editButton);
        logoutButton = findViewById(R.id.logoutButton);
        paymentButton = findViewById(R.id.PaymentButton);
        backArrowText = findViewById(R.id.backArrowText);
        profileImage = findViewById(R.id.profileImage);
        editProfileImage = findViewById(R.id.editProfileImage);

        // Get current user
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Load user details from SharedPreferences or Firebase (if logged in)
        loadUserProfile(currentUser);

        // Edit button click event
        editButton.setOnClickListener(v -> toggleEditProfile());

        // Logout button click event
        logoutButton.setOnClickListener(v -> logoutUser());

        // Payment button click event
        paymentButton.setOnClickListener(v -> openPaymentActivity());

        // Back button click event
        backArrowText.setOnClickListener(v -> onBackPressed());

        // Profile image edit click event
        editProfileImage.setOnClickListener(v -> openImagePicker());
    }

    private void loadUserProfile(FirebaseUser currentUser) {
        if (currentUser != null) {
            // Fetch data from Firebase or SharedPreferences
            fullName.setText(currentUser.getDisplayName());
            email.setText(currentUser.getEmail());
            // Retrieve other fields if necessary (Phone, Vehicle, etc.)
            // For now, we only display the user's Firebase data.
        }

        // Set fields to non-editable mode initially
        setFieldsEnabled(false);
    }

    private void toggleEditProfile() {
        isEditing = !isEditing;

        if (isEditing) {
            // Enable Editing
            setFieldsEnabled(true);
            editButton.setText("Save");
        } else {
            // Save Changes
            saveUserProfile();
            setFieldsEnabled(false);
            editButton.setText("Edit Profile");
        }
    }

    private void saveUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fullName", fullName.getText().toString());
        editor.putString("phoneNumber", phoneNumber.getText().toString());
        editor.putString("email", email.getText().toString());
        editor.putString("vehicle", vehicle.getText().toString());
        editor.apply();
    }

    private void setFieldsEnabled(boolean enabled) {
        fullName.setEnabled(enabled);
        phoneNumber.setEnabled(enabled);
        email.setEnabled(enabled);
        vehicle.setEnabled(enabled);
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear saved user data
        editor.apply();

        // Redirect to Login/Register Activity
        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void openPaymentActivity() {
        Intent intent = new Intent(UserProfileActivity.this, PaymentActivity.class);
        startActivity(intent);
    }

    private void openImagePicker() {
        // Open image picker to allow the user to update their profile picture
        // This could be implemented using an Intent to pick an image from the gallery or camera
    }
}
