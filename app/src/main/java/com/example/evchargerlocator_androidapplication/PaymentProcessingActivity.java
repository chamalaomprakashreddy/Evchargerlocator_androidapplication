package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentProcessingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        TextView statusText = findViewById(R.id.paymentStatusText);
        statusText.setText("Processing your payment...");

        // Simulate payment processing (3 seconds delay)
        new android.os.Handler().postDelayed(() -> statusText.setText("Payment Successful!"), 3000);
    }
}
