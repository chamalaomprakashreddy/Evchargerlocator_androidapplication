package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CardDetails extends AppCompatActivity {

    private EditText cardNumberInput, cardHolderInput, expiryDateInput, cvvInput;
    private Button continueButton;
    private CheckBox saveCardCheckbox;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("SavedCards");

        // Initialize UI elements
        cardNumberInput = findViewById(R.id.cardNumberInput);
        cardHolderInput = findViewById(R.id.cardHolderInput);
        expiryDateInput = findViewById(R.id.expiryDateInput);
        cvvInput = findViewById(R.id.cvvInput);
        continueButton = findViewById(R.id.continueButton);
        saveCardCheckbox = findViewById(R.id.saveCardCheckbox);
        TextView backArrowText = findViewById(R.id.backArrowText);

        // Back button functionality
        backArrowText.setOnClickListener(v -> finish());

        // Continue button functionality
        continueButton.setOnClickListener(v -> saveCardDetails());
    }

    private void saveCardDetails() {
        String userId = auth.getCurrentUser().getUid();
        String cardNumber = cardNumberInput.getText().toString().trim();
        String cardHolderName = cardHolderInput.getText().toString().trim();
        String expiryDate = expiryDateInput.getText().toString().trim();
        String cvv = cvvInput.getText().toString().trim();

        // Validate inputs
        if (cardNumber.isEmpty() || cardHolderName.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cardNumber.length() < 16) {
            Toast.makeText(this, "Enter a valid 16-digit card number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!expiryDate.matches("(0[1-9]|1[0-2])/(\\d{2})")) {
            Toast.makeText(this, "Enter a valid expiry date (MM/YY)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cvv.length() < 3) {
            Toast.makeText(this, "Enter a valid 3-digit CVV", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mask the card number (Only show last 4 digits)
        String maskedCardNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);

        if (saveCardCheckbox.isChecked()) {
            String cardId = databaseRef.child(userId).push().getKey();
            Card card = new Card(maskedCardNumber, cardHolderName, expiryDate);

            // Save the card details to Firebase
            databaseRef.child(userId).child(cardId).setValue(card).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(CardDetails.this, "Card saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CardDetails.this, "Failed to save card", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Redirect to PaymentProcessingActivity
        Intent intent = new Intent(CardDetails.this, PaymentProcessingActivity.class);
        startActivity(intent);
        finish();
    }
}
