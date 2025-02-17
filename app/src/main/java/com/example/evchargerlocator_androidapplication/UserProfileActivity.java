package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
    private Button logoutButton, paymentButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Views
        fullName = findViewById(R.id.fullName);
        phoneNumber = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.email);
        vehicle = findViewById(R.id.vehicle);
        logoutButton = findViewById(R.id.logoutButton);
        paymentButton = findViewById(R.id.PaymentButton);

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

        logoutButton.setOnClickListener(v -> logoutUser());
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void openPaymentActivity() {
        startActivity(new Intent(this, PaymentActivity.class));
    }
}
