package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

public class CardDetails extends AppCompatActivity {
    private EditText cardNumber, expiryDate, cvv, cardHolderName;
    private Button continueButton;
    private ImageView showHideCvv;
    private CheckBox saveCardCheckBox;
    private boolean isCvvVisible = false;
    private static Set<String> savedCards = new HashSet<>();

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

        backArrowText.setOnClickListener(v -> finish());

        showHideCvv.setOnClickListener(v -> {
            if (isCvvVisible) {
                cvv.setInputType(129);
                showHideCvv.setImageResource(R.drawable.ic_eye_closed);
            } else {
                cvv.setInputType(145);
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
        String cardNum = cardNumber.getText().toString().trim();
        String expDate = expiryDate.getText().toString().trim();
        String cvvCode = cvv.getText().toString().trim();
        String cardHolder = cardHolderName.getText().toString().trim();

        if (cardNum.length() < 12 || cardNum.length() > 19) {
            Toast.makeText(this, "Invalid Card Number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!expDate.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            Toast.makeText(this, "Invalid Expiry Date (MM/YY)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cvvCode.length() < 3 || cvvCode.length() > 4) {
            Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show();
            return;
        }

        if (saveCardCheckBox.isChecked()) {
            String maskedCard = cardNum.substring(0, 4) + " •••• " + cardNum.substring(cardNum.length() - 4);
            savedCards.add(maskedCard);
            Toast.makeText(this, "Card Saved Successfully", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(CardDetails.this, PaymentProcessingActivity.class);
        startActivity(intent);
    }

    public static Set<String> getSavedCards() {
        return savedCards;
    }
}