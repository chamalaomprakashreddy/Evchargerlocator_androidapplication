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

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPassword;
    private TextView backArrowText;
    private ImageView showHidePasswordButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        
        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        backArrowText = findViewById(R.id.backArrowText);
        showHidePasswordButton = findViewById(R.id.showHidePasswordButton);

        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen

        // Toggle password visibility
        showHidePasswordButton.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showHidePasswordButton.setImageResource(R.drawable.ic_eye_closed);
            } else {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showHidePasswordButton.setImageResource(R.drawable.ic_eye_open);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Set up the login button functionality
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Admin login logic (use hardcoded credentials)
                if (isAdmin(username, password)) {
                    Toast.makeText(AdminLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                } else {
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
        return username.equals("evlocator1834@gmail.com") && password.equals("Admin@1834");
    }
}