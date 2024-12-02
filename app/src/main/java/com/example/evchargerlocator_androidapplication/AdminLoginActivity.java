package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView forgotPassword;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        backArrow = findViewById(R.id.backArrow);

        // Set up the back arrow functionality
        backArrow.setOnClickListener(v -> {
            finish(); // Go back to the previous screen (or close activity)
        });

        // Set up the login button functionality
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Admin login logic (use hardcoded credentials)
                if (isAdmin(username, password)) {
                    // Successful admin login
                    Toast.makeText(AdminLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    // Navigate to the admin dashboard or another activity
                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                } else {
                    // Login failed
                    Toast.makeText(AdminLoginActivity.this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup Password Intent
        forgotPassword.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(AdminLoginActivity.this, ForgetPasswordActivity.class);
            startActivity(forgotPasswordIntent);
        });
    }

    private boolean isAdmin(String username, String password) {
        // Hardcoded admin credentials
        return username.equals("evlocator1834@gmail.com") && password.equals("Admin@1834");
    }
}
