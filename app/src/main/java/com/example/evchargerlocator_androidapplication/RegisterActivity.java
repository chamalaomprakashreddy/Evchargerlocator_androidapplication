package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerFullName, registerEmail, registerPhoneNumber, registerPassword, confirmPassword, registerVehicle, adminKey;
    private TextView passwordErrorText, alreadyHaveAccount, backArrowText;
    private Button registerButton;
    private CheckBox adminCheckBox;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private ImageView togglePasswordVisibility, toggleConfirmPasswordVisibility;

    private static final String ADMIN_SECRET_KEY = "EV_ADMIN_2025";

    // Updated Email Regex: At least 1 uppercase, 1 lowercase, 1 number, and 1 special character
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        registerFullName = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        registerPassword = findViewById(R.id.registerPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerVehicle = findViewById(R.id.registerVehicle);
        passwordErrorText = findViewById(R.id.passwordErrorText);
        registerButton = findViewById(R.id.registerButton);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
        backArrowText = findViewById(R.id.backArrowText);
        adminCheckBox = findViewById(R.id.adminCheckBox);
        adminKey = findViewById(R.id.adminKey);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);

        backArrowText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        adminCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adminKey.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            registerVehicle.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        registerButton.setOnClickListener(v -> handleRegistration());

        alreadyHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

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

        if (!isAdmin && vehicle.isEmpty()) {
            Toast.makeText(this, "Please enter vehicle details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d{10}")) {
            registerPhoneNumber.setError("Phone number must be exactly 10 digits");
            return;
        }

        // **Email Validation**
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !EMAIL_PATTERN.matcher(email).matches()) {
            registerEmail.setError("Invalid email format! Must contain lowercase, 1 number, and 1 special character.");
            return;
        }

        // **Password Validation**
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
                            sendEmailVerification(user, fullName, email, phoneNumber, vehicle, role);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user, String fullName, String email, String phoneNumber, String vehicle, String role) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveUserToDatabase(user, fullName, email, phoneNumber, vehicle, role);
                Toast.makeText(RegisterActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                firebaseAuth.signOut();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToDatabase(FirebaseUser user, String fullName, String email, String phoneNumber, String vehicle, String role) {
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phoneNumber", phoneNumber);
        userData.put("vehicle", vehicle);
        userData.put("role", role);
        userData.put("emailVerified", false);

        userRef.setValue(userData);
    }
}
