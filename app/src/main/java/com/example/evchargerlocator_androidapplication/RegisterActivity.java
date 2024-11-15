package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerUsername, registerEmail, registerPassword;
    private Button registerButton;
    private TextView passwordErrorText; // TextView to show password error

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerUsername = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.registerButton);
        passwordErrorText = findViewById(R.id.passwordErrorText); // Reference to the password error TextView

        // Handle Register Button Click
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = registerUsername.getText().toString();
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();

                // Hide password error text by default when user starts typing a valid password
                passwordErrorText.setVisibility(View.GONE);

                // Validate Email Format
                if (!isValidEmail(email)) {
                    Toast.makeText(RegisterActivity.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate Password
                if (!isValidPassword(password)) {
                    passwordErrorText.setVisibility(View.VISIBLE); // Show the password format error message
                    return;
                }

                // Simple validation (you can enhance this logic)
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    // Assuming registration is successful (you can integrate with a real backend)
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                    // Redirect to login after successful registration
                    finish();  // Close RegisterActivity
                }
            }
        });
    }

    // Email Validation method
    public boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Password Validation method
    public boolean isValidPassword(String password) {
        // Regular expression for password complexity:
        // At least one uppercase letter, one lowercase letter, one number, one symbol, and minimum 8 characters
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>?/])(?=.{8,})$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
