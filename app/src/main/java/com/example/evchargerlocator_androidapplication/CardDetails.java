package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;

public class CardDetails extends AppCompatActivity {
    private EditText cardNumber, expiryDate, cvv, cardHolderName;
    private Button continueButton;
    private ImageView showHideCvv;
    private CheckBox saveCardCheckBox;
    private boolean isCvvVisible = false;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        cardNumber = findViewById(R.id.cardNumber);
        expiryDate = findViewById(R.id.expiryDate);
        cvv = findViewById(R.id.cvv);
        cardHolderName = findViewById(R.id.cardHolderName);
        continueButton = findViewById(R.id.continueButton);
        showHideCvv = findViewById(R.id.showHideCvv);
        saveCardCheckBox = findViewById(R.id.saveCardCheckbox);
        TextView backArrowText = findViewById(R.id.backArrowText);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("SavedCards");

        backArrowText.setOnClickListener(v -> finish());

        showHideCvv.setOnClickListener(v -> {
            if (isCvvVisible) {
                cvv.setInputType(129); // Hide CVV
                showHideCvv.setImageResource(R.drawable.ic_eye_closed);
            } else {
                cvv.setInputType(145); // Show CVV
                showHideCvv.setImageResource(R.drawable.ic_eye_open);
            }
            isCvvVisible = !isCvvVisible;
            cvv.setSelection(cvv.getText().length());
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        cardNumber.addTextChangedListener(textWatcher);
        expiryDate.addTextChangedListener(textWatcher);
        cvv.addTextChangedListener(textWatcher);
        cardHolderName.addTextChangedListener(textWatcher);

        continueButton.setOnClickListener(v -> handlePayment());
    }

    private void validateFields() {
        boolean isValid = !cardNumber.getText().toString().trim().isEmpty()
                && !expiryDate.getText().toString().trim().isEmpty()
                && !cvv.getText().toString().trim().isEmpty()
                && !cardHolderName.getText().toString().trim().isEmpty();
        continueButton.setEnabled(isValid);
    }

    private void handlePayment() {
        String userId = auth.getCurrentUser().getUid();
        String cardNum = cardNumber.getText().toString().trim();
        String expDate = expiryDate.getText().toString().trim();
        String cvvCode = cvv.getText().toString().trim();
        String cardHolder = cardHolderName.getText().toString().trim();

        // Validate Card Number Length
        if (cardNum.length() < 12 || cardNum.length() > 19) {
            Toast.makeText(this, "Invalid Card Number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Expiry Date Format (MM/YY) and Expiry Year
        if (!expDate.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            Toast.makeText(this, "Invalid Expiry Date (MM/YY)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract month and year from expiry date
        String[] parts = expDate.split("/");
        int expMonth = Integer.parseInt(parts[0]);
        int expYear = Integer.parseInt(parts[1]) + 2000; // Convert YY to YYYY

        // Get current year and month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based

        // Ensure expiry date is in the future
        if (expYear < currentYear || (expYear == currentYear && expMonth < currentMonth)) {
            Toast.makeText(this, "Expiry date must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate CVV
        if (cvvCode.length() < 3 || cvvCode.length() > 4) {
            Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save card if checkbox is checked
        if (saveCardCheckBox.isChecked()) {
            String maskedCard = cardNum.substring(0, 4) + " •••• " + cardNum.substring(cardNum.length() - 4);
            Card card = new Card(maskedCard, expDate, cardHolder);
            String cardId = databaseRef.child(userId).push().getKey();

            databaseRef.child(userId).child(cardId).setValue(card).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Card Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save card", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Proceed to payment processing screen
        Intent intent = new Intent(CardDetails.this, PaymentProcessingActivity.class);
        startActivity(intent);
    }
}
