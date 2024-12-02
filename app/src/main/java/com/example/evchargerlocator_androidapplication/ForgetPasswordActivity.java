package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    private EditText emailInput;
    private Button submitButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        submitButton = findViewById(R.id.submitButton);

        // Set onClickListener for the Submit button
        submitButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(ForgetPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordResetEmail(email);
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgetPasswordActivity.this, "Password reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    } else {
                        Toast.makeText(ForgetPasswordActivity.this, "Failed to send reset email. Please check the email address.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}