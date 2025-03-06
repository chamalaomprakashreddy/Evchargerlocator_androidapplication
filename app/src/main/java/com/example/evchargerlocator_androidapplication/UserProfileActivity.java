package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    private EditText fullName, phoneNumber, email, vehicle;
    private Button editButton, paymentButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef, onlineStatusRef;
    private boolean isEditing = false; // Track edit mode
    private String userId;

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

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            onlineStatusRef = userRef.child("lastSeen");

            showUserData();
            setUserOnline();  // ✅ Set user as online when activity is opened
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        backArrowText.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, HomePageActivity.class));
            finish();
        });

        editButton.setOnClickListener(v -> toggleEditProfile());
        paymentButton.setOnClickListener(v -> openPaymentActivity());
    }

    public void showUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fullName.setText(snapshot.child("fullName").getValue(String.class));
                    email.setText(snapshot.child("email").getValue(String.class));
                    phoneNumber.setText(snapshot.child("phoneNumber").getValue(String.class));
                    vehicle.setText(snapshot.child("vehicle").getValue(String.class));
                }
                setFieldsEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditProfile() {
        isEditing = !isEditing;

        if (isEditing) {
            setFieldsEnabled(true);
            editButton.setText("Save");
        } else {
            saveUserProfile();
            setFieldsEnabled(false);
            editButton.setText("Edit Profile");
        }
    }

    private void saveUserProfile() {
        String updatedFullName = fullName.getText().toString().trim();
        String updatedPhoneNumber = phoneNumber.getText().toString().trim();
        String updatedVehicle = vehicle.getText().toString().trim();

        if (updatedFullName.isEmpty() || updatedPhoneNumber.isEmpty() || updatedVehicle.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("fullName").setValue(updatedFullName);
        userRef.child("phoneNumber").setValue(updatedPhoneNumber);
        userRef.child("vehicle").setValue(updatedVehicle);

        Toast.makeText(UserProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void setFieldsEnabled(boolean enabled) {
        fullName.setEnabled(enabled);
        phoneNumber.setEnabled(enabled);
        vehicle.setEnabled(enabled);
    }

    private void openPaymentActivity() {
        startActivity(new Intent(UserProfileActivity.this, PaymentActivity.class));
    }

    private void setUserOnline() {
        if (onlineStatusRef != null) {
            onlineStatusRef.setValue("Online");
        }
    }

    private void setUserOffline() {
        if (onlineStatusRef != null) {
            String lastSeenTime = String.valueOf(System.currentTimeMillis());
            onlineStatusRef.setValue(lastSeenTime);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserOffline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserOnline();
    }
}
