package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerFullName, registerEmail, registerPhoneNumber, registerPassword, confirmPassword, registerVehicle, adminKey;
    private TextView passwordErrorText, alreadyHaveAccount;
    private Button registerButton;
    private CheckBox adminCheckBox;
    private FirebaseAuth firebaseAuth;
    private ImageView togglePasswordVisibility, toggleConfirmPasswordVisibility;
    private boolean isPasswordVisible = false, isConfirmPasswordVisible = false;

    private static final String ADMIN_SECRET_KEY = "EV_ADMIN_2025"; // Change to a secure key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        registerFullName = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        registerPassword = findViewById(R.id.registerPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerVehicle = findViewById(R.id.registerVehicle);
        passwordErrorText = findViewById(R.id.passwordErrorText);
        registerButton = findViewById(R.id.registerButton);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
        adminCheckBox = findViewById(R.id.adminCheckBox);
        adminKey = findViewById(R.id.adminKey);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);
        TextView backArrowText = findViewById(R.id.backArrowText);

        // Back Arrow Functionality
        backArrowText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Show/Hide Admin Key field
        adminCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adminKey.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            registerVehicle.setVisibility(isChecked ? View.GONE : View.VISIBLE); // Hide vehicle field for admins
        });

        // Register Button Click Listener
        registerButton.setOnClickListener(v -> handleRegistration());

        // Redirect to Login Page
        alreadyHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        // Toggle password visibility
        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility(registerPassword, togglePasswordVisibility));
        toggleConfirmPasswordVisibility.setOnClickListener(v -> togglePasswordVisibility(confirmPassword, toggleConfirmPasswordVisibility));
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getInputType() == (InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT)) {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye_open);
        } else {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            toggleIcon.setImageResource(R.drawable.ic_eye_closed);
        }
        passwordField.setSelection(passwordField.getText().length());
    }

    private void handleRegistration() {
        String fullName = registerFullName.getText().toString().trim();
        String email = registerEmail.getText().toString().trim();
        String phoneNumber = registerPhoneNumber.getText().toString().trim();
        String vehicle = registerVehicle.getText().toString().trim();
        String password = registerPassword.getText().toString();
        String confirmPass = confirmPassword.getText().toString();
        boolean isAdmin = adminCheckBox.isChecked();
        String adminCode = adminKey.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "All fields must be filled in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Admins do not need vehicle details, but users must provide one
        if (!isAdmin && vehicle.isEmpty()) {
            Toast.makeText(this, "Please enter vehicle details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            passwordErrorText.setVisibility(View.VISIBLE);
            return;
        } else {
            passwordErrorText.setVisibility(View.GONE);
        }

        if (isAdmin && !adminCode.equals(ADMIN_SECRET_KEY)) {
            Toast.makeText(this, "Invalid Admin Key!", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = isAdmin ? "admin" : "user";

        // If admin, set vehicle to "N/A"
        if (isAdmin) {
            vehicle = "N/A";
        }

        registerUserWithFirebase(fullName, email, phoneNumber, vehicle, password, role);
    }

    private void registerUserWithFirebase(String fullName, String email, String phoneNumber, String vehicle, String password, String role) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(role.equals("admin") ? "admins" : "users").child(userId);

                            // Save user details in Firebase
                            User userData = new User(email, fullName, phoneNumber, vehicle, role);

                            userRef.setValue(userData).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Failed to store user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}