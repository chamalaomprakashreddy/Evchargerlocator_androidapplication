package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, adminLoginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private FirebaseAuth firebaseAuth;
    private ImageView showHidePasswordButton;
    private boolean isPasswordVisible = false;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        showHidePasswordButton = findViewById(R.id.showHidePasswordButton);

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

        // Login Button Logic
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                checkUserInDatabase(user.getUid());
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    });
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

        // ForgotPasswordActivity Navigation
        forgotPasswordTextView.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(MainActivity.this, ForgetPasswordActivity.class);
            startActivity(forgotPasswordIntent);
        });
    }

    private void checkUserInDatabase(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // The user exists in the "users" collection -> Proceed to Homepage
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
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