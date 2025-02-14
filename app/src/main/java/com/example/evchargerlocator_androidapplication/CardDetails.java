package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CardDetails extends AppCompatActivity {
    private EditText cardNumber, expiryDate, cvv, cardHolderName;
    private Button saveCardButton;
    private ImageView showHideCvv;
    private boolean isCvvVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        cardNumber = findViewById(R.id.cardNumber);
        expiryDate = findViewById(R.id.expiryDate);
        cvv = findViewById(R.id.cvv);
        cardHolderName = findViewById(R.id.cardHolderName);
        saveCardButton = findViewById(R.id.saveCardButton);
        showHideCvv = findViewById(R.id.showHideCvv);
        TextView backArrowText = findViewById(R.id.backArrowText);

        // Back navigation
        backArrowText.setOnClickListener(v -> finish());

        // Show/Hide CVV functionality
        showHideCvv.setOnClickListener(v -> {
            if (isCvvVisible) {
                cvv.setInputType(129); // numberPassword type
                showHideCvv.setImageResource(R.drawable.ic_eye_closed);
            } else {
                cvv.setInputType(145); // visible password type
                showHideCvv.setImageResource(R.drawable.ic_eye_open);
            }
            isCvvVisible = !isCvvVisible;
            cvv.setSelection(cvv.getText().length());
        });

        // Enable Save button only when all fields are filled
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

        saveCardButton.setOnClickListener(v -> saveCard());
    }

    private void validateFields() {
        String cardNum = cardNumber.getText().toString().trim();
        String expDate = expiryDate.getText().toString().trim();
        String cvvCode = cvv.getText().toString().trim();
        String cardHolder = cardHolderName.getText().toString().trim();

        saveCardButton.setEnabled(!cardNum.isEmpty() && !expDate.isEmpty() && !cvvCode.isEmpty() && !cardHolder.isEmpty());
    }

    private void saveCard() {
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

        Toast.makeText(this, "Card Saved Successfully", Toast.LENGTH_SHORT).show();
    }
}
