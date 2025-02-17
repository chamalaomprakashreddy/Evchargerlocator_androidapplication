package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SavedPaymentMethodsActivity extends AppCompatActivity {

    private RecyclerView paymentMethodsRecyclerView;
    private Button continueButton;
    private List<String> paymentMethods;
    private PaymentMethodsAdapter adapter;
    private String selectedMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_payment_methods);

        paymentMethodsRecyclerView = findViewById(R.id.paymentMethodsRecyclerView);
        continueButton = findViewById(R.id.continueButton);
        TextView backArrowText = findViewById(R.id.backArrowText);

        backArrowText.setOnClickListener(v -> finish());

        Set<String> savedCards = CardDetails.getSavedCards();
        paymentMethods = new ArrayList<>(savedCards);

        adapter = new PaymentMethodsAdapter(paymentMethods, selected -> {
            selectedMethod = selected;
            continueButton.setEnabled(true);
        });

        paymentMethodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodsRecyclerView.setAdapter(adapter);

        continueButton.setEnabled(false);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(SavedPaymentMethodsActivity.this, PaymentProcessingActivity.class);
            startActivity(intent);
        });
    }
}
