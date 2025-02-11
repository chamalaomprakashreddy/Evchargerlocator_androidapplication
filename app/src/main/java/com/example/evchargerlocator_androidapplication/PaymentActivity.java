package com.example.evchargerlocator_androidapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        TextView backArrowText = findViewById(R.id.backArrowText);
        Button googlePayButton = findViewById(R.id.googlePayButton);
        Button paypalButton = findViewById(R.id.paypalButton);
        TextView addPaymentMethodText = findViewById(R.id.addPaymentMethodText);
        TextView savedPaymentMethodText = findViewById(R.id.savedPaymentMethodText);

        // Navigate to Profile when clicking back arrow
        backArrowText.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Open Saved Payment Method Activity
        savedPaymentMethodText.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, SavedPaymentMethodsActivity.class);
            startActivity(intent);
        });

        // Navigate to Card Details when clicking "Add Payment Method"
        addPaymentMethodText.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, CardDetails.class);
            startActivity(intent);
        });

        // Open Google Pay Wallet
        googlePayButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://pay.google.com/"));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(PaymentActivity.this, "Google Pay is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Open PayPal App
        paypalButton.setOnClickListener(v -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.paypal.android.p2pmobile");
            if (intent != null) {
                startActivity(intent);
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/")));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(PaymentActivity.this, "PayPal app not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
