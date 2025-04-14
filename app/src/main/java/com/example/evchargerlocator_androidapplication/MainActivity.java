package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;

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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // Unique request code

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

        // ✅ Password visibility toggle
        showHidePasswordButton.setOnClickListener(v -> togglePasswordVisibility());

        // ✅ Login Button Click
        loginButton.setOnClickListener(v -> loginUser());

        // ✅ Navigation: Register Page
        registerTextView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        // ✅ Navigation: Admin Login
        adminLoginButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminLoginActivity.class)));

        // ✅ Navigation: Forgot Password
        forgotPasswordTextView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ForgetPasswordActivity.class)));

        // ✅ Check and request location permission
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            Log.d(TAG, "✅ Location permission already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "✅ Location permission granted by user");
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied. Some features may be limited.",
                        Toast.LENGTH_LONG).show();
                Log.w(TAG, "⚠️ Location permission denied by user");
            }
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            showHidePasswordButton.setImageResource(R.drawable.ic_eye_closed);
        } else {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showHidePasswordButton.setImageResource(R.drawable.ic_eye_open);
        }
        passwordEditText.setSelection(passwordEditText.getText().length()); // Keep cursor at the end
        isPasswordVisible = !isPasswordVisible;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // ✅ Input Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Show progress and disable login button
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                checkUserInDatabase(user.getUid());
                            } else {
                                firebaseAuth.signOut();
                                Toast.makeText(MainActivity.this, "Email not verified! Check your email.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);
                            }
                        }
                    } else {
                        // ✅ Handle login failure
                        Log.e(TAG, "Login failed: ", task.getException());
                        Toast.makeText(MainActivity.this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                    }
                });
    }

    private void checkUserInDatabase(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);

                if (snapshot.exists()) {
                    // ✅ User exists -> Proceed to Homepage
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToHomePage();
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String token = task.getResult();
                                    usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("deviceToken")
                                            .setValue(token)
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ FCM token saved to DB"))
                                            .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to save FCM token", e));
                                } else {
                                    Log.w(TAG, "⚠️ Failed to get FCM token", task.getException());
                                }
                            });

                } else {
                    // ❌ Unauthorized user
                    firebaseAuth.signOut();
                    Toast.makeText(MainActivity.this, "Unauthorized! Only registered users can log in.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ✅ Handle database error
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                Toast.makeText(MainActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(MainActivity.this, HomePageActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}