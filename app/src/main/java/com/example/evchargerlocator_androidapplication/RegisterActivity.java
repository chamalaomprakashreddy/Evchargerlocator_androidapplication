package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // Regular Expression for Password Validation
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    private EditText usernameEditText, emailEditText, passwordEditText;
    private TextView passwordErrorText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Views
        usernameEditText = findViewById(R.id.registerUsername);
        emailEditText = findViewById(R.id.registerEmail);
        passwordEditText = findViewById(R.id.registerPassword);
        passwordErrorText = findViewById(R.id.passwordErrorText);
        registerButton = findViewById(R.id.registerButton);

        // Set up the Register Button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });
    }

    private void handleRegistration() {
        // Get Input Values
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate Inputs
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            return;
        }

        if (!isPasswordValid(password)) {
            passwordErrorText.setVisibility(View.VISIBLE);
            return;
        } else {
            passwordErrorText.setVisibility(View.GONE);
        }

        // Proceed with Registration (Placeholder for actual implementation)
        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

        // Optionally clear the input fields after successful registration
        clearInputFields();
    }

    private boolean isPasswordValid(String password) {
        // Check if the password matches the regex
        return Pattern.compile(PASSWORD_PATTERN).matcher(password).matches();
    }

    private void clearInputFields() {
        usernameEditText.setText("");
        emailEditText.setText("");
        passwordEditText.setText("");
    }
}
