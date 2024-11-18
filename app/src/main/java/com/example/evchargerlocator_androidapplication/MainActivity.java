package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, adminLoginButton;
    private TextView registerTextView;

    // Hardcoded credentials for demonstration
    private final String validEmail = "user@example.com";
    private final String validPassword = "password123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        adminLoginButton = findViewById(R.id.adminLoginButton);

        // Login Button Logic
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.equals(validEmail) && password.equals(validPassword)) {
                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                // Navigate to user dashboard or similar
            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

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
    }
}
