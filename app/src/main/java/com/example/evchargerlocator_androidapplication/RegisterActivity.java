package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerUsername, registerEmail, registerPhoneNumber, registerPassword;
    private TextView passwordErrorText, alreadyHaveAccount;
    private Button registerButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        registerUsername = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        registerPassword = findViewById(R.id.registerPassword);
        passwordErrorText = findViewById(R.id.passwordErrorText);
        registerButton = findViewById(R.id.registerButton);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);

        // Set up the SignUp button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        // Optionally, set up "Already have an account?" click listener (for login screen)
        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to the login screen if the user already has an account
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish this activity to prevent the user from going back to the registration screen
            }
        });
    }

    private void handleRegistration() {
        String username = registerUsername.getText().toString();
        String email = registerEmail.getText().toString();
        String phoneNumber = registerPhoneNumber.getText().toString();
        String password = registerPassword.getText().toString();

        // Simple validation logic (you can customize further)
        if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields must be filled in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password validation
        if (!isPasswordValid(password)) {
            passwordErrorText.setVisibility(View.VISIBLE);
        } else {
            passwordErrorText.setVisibility(View.GONE);

            // If all fields are valid, proceed with Firebase registration
            registerUserWithFirebase(email, password);
        }
    }

    // Password validation: checks if it contains at least one uppercase letter, one lowercase letter, one symbol,
    // one number, and is at least 8 characters long
    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&  // at least one uppercase letter
                password.matches(".*[a-z].*") &&  // at least one lowercase letter
                password.matches(".*\\d.*") &&    // at least one digit
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"); // at least one special character
    }

    private void registerUserWithFirebase(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Show success message
                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                            // Redirect to MainActivity (home page)
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();  // Finish the registration activity to prevent the user from navigating back to it
                        }
                    } else {
                        // Handle errors (e.g., email already registered)
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
