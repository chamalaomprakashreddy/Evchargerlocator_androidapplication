package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.*;

import com.google.firebase.database.*;

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
                            Log.d(TAG, "User logged in successfully: " + user.getEmail());
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
