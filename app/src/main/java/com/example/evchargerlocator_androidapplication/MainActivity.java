package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);

        // Handle Login Button Click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                // Email Validation
                if (!isValidEmail(email)) {
                    Toast.makeText(MainActivity.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Simple validation (you can enhance this logic)
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in both fields.", Toast.LENGTH_SHORT).show();
                } else {
                    // Assuming login is successful (you can integrate with a real backend)
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Start the main activity or dashboard after login
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Open RegisterActivity when the user clicks the link
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterActivity();
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

    public void openRegisterActivity() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
