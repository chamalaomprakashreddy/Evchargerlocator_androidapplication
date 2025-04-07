package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class AdminLoginActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText usernameInput;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPassword;
    private TextView backArrowText;
    private ImageView showHidePasswordButton;

    // Password visibility toggle
    private boolean isPasswordVisible = false;

    // Firebase authentication and database references
    private FirebaseAuth firebaseAuth;
    private DatabaseReference adminRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize Firebase authentication and reference to "admins" node
        firebaseAuth = FirebaseAuth.getInstance();
        adminRef = FirebaseDatabase.getInstance().getReference("admins");

        // Link UI elements with layout IDs
        usernameInput = findViewById(R.id.usernameInput);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        backArrowText = findViewById(R.id.backArrowText);
        showHidePasswordButton = findViewById(R.id.showHidePasswordButton);

        // Back button navigates to previous screen
        backArrowText.setOnClickListener(v -> finish());

        // Toggle password visibility on eye icon click
        showHidePasswordButton.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showHidePasswordButton.setImageResource(R.drawable.ic_eye_closed);
            } else {
                // Show password
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showHidePasswordButton.setImageResource(R.drawable.ic_eye_open);
            }

            // Maintain cursor at end of text
            passwordEditText.setSelection(passwordEditText.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Login button logic
        loginButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Check for empty fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Authenticate user
                authenticateAdmin(email, password);
            }
        });

        // Forgot password text redirects to reset activity
        forgotPassword.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(AdminLoginActivity.this, ForgetPasswordActivity.class);
            startActivity(forgotPasswordIntent);
        });
    }

    // Authenticate user credentials with Firebase
    private void authenticateAdmin(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User signed in successfully
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Check if user exists in 'admins' node
                            checkAdminInDatabase(user.getUid(), email);
                        }
                    } else {
                        // Authentication failed
                        Toast.makeText(AdminLoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Verify if signed-in user is an admin by checking the 'admins' node
    private void checkAdminInDatabase(String userId, String email) {
        adminRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User is a valid admin, proceed to admin dashboard
                    Toast.makeText(AdminLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                    intent.putExtra("adminEmail", email);
                    startActivity(intent);
                    finish(); // Close login activity
                } else {
                    // Not authorized as admin
                    firebaseAuth.signOut(); // Sign out non-admin user
                    Toast.makeText(AdminLoginActivity.this, "Unauthorized! Not an admin.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database read error
                Toast.makeText(AdminLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
