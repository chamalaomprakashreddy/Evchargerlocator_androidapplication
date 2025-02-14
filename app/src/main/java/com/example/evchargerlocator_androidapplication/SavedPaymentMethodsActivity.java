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

public class SavedPaymentMethodsActivity extends AppCompatActivity {

    private RecyclerView paymentMethodsRecyclerView;
    private Button paypalButton;
    private List<String> savedPaymentMethods;
    private PaymentMethodsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_payment_methods);

        paymentMethodsRecyclerView = findViewById(R.id.paymentMethodsRecyclerView);
        paypalButton = findViewById(R.id.paypalButton);
        TextView backArrowText = findViewById(R.id.backArrowText);

        // Back navigation
        backArrowText.setOnClickListener(v -> finish());

        // Dummy data for saved payment methods
        savedPaymentMethods = new ArrayList<>();
        savedPaymentMethods.add("Visa •••• 1234");
        savedPaymentMethods.add("MasterCard •••• 5678");
        savedPaymentMethods.add("Amex •••• 9012");

        // Set up RecyclerView
        adapter = new PaymentMethodsAdapter(savedPaymentMethods, this::onPaymentMethodSelected);
        paymentMethodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodsRecyclerView.setAdapter(adapter);

        // Handle PayPal button click
        paypalButton.setOnClickListener(v -> {
            Intent intent = new Intent(SavedPaymentMethodsActivity.this, PaymentProcessingActivity.class);
            startActivity(intent);
        });
    }

    private void onPaymentMethodSelected(String method) {
        Intent intent = new Intent(SavedPaymentMethodsActivity.this, CardDetails.class);
        intent.putExtra("SelectedPaymentMethod", method);
        startActivity(intent);
    }
}
