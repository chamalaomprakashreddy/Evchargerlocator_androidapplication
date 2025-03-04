package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, adminLoginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private ImageView showHidePasswordButton;
    private boolean isPasswordVisible = false;

    private DatabaseReference usersRef;

    private static final String TAG = "MainActivity"; // For debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        showHidePasswordButton = findViewById(R.id.showHidePasswordButton);
        progressBar = findViewById(R.id.progressBar);

        // Toggle password visibility
        showHidePasswordButton.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showHidePasswordButton.setImageResource(R.drawable.ic_eye_closed);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showHidePasswordButton.setImageResource(R.drawable.ic_eye_open);
            }
            passwordEditText.setSelection(passwordEditText.length()); // Keep cursor at the end
            isPasswordVisible = !isPasswordVisible;
        });

        // Login Button Click
        loginButton.setOnClickListener(v -> loginUser());

        // RegisterActivity Navigation
        registerTextView.setOnClickListener(v -> {
            Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });

        // AdminLoginActivity Navigation
        adminLoginButton.setOnClickListener(v -> {
            Intent adminIntent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(adminIntent);
        });

        // ForgotPasswordActivity Navigation
        forgotPasswordTextView.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(MainActivity.this, ForgetPasswordActivity.class);
            startActivity(forgotPasswordIntent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Input Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress while logging in
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // ✅ Commented out email verification check
                            /*
                            if (!user.isEmailVerified()) {
                                Toast.makeText(MainActivity.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                firebaseAuth.signOut(); // Log out unverified users
                                return;
                            }
                            */

                            checkUserInDatabase(user.getUid());
                        }
                    } else {
                        Log.e(TAG, "Login failed: ", task.getException());
                        Toast.makeText(MainActivity.this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInDatabase(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // The user exists in the database -> Proceed to Homepage
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // User is not found in "users" collection -> Log out
                    firebaseAuth.signOut();
                    Toast.makeText(MainActivity.this, "Unauthorized! Only registered users can log in.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
