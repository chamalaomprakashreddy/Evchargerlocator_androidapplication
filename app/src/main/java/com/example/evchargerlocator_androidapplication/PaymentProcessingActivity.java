package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

public class PaymentProcessingActivity extends AppCompatActivity {

    private TextView totalAmountText, usedEnergyText, chargingTimeText, chargerIdText;
    private ImageView thumbsUp, thumbsDown;
    private Button homeButton;

    private String chargingLevel, connectorType, network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        totalAmountText = findViewById(R.id.totalAmount);
        usedEnergyText = findViewById(R.id.usedEnergyText);
        chargingTimeText = findViewById(R.id.chargingTimeText);
        chargerIdText = findViewById(R.id.chargerIdText);
        thumbsUp = findViewById(R.id.thumbsUp);
        thumbsDown = findViewById(R.id.thumbsDown);
        homeButton = findViewById(R.id.homeButton);

        // Get extras from Intent
        Intent intent = getIntent();
        chargingLevel = intent.getStringExtra("chargingLevel");
        connectorType = intent.getStringExtra("connectorType");
        network = intent.getStringExtra("network");

        // Simulate payment processing delay
        new Handler().postDelayed(this::displayChargingDetails, 3000);

        // Feedback handling
        thumbsUp.setOnClickListener(v -> showFeedbackPopup(true));
        thumbsDown.setOnClickListener(v -> showFeedbackPopup(false));

        // Navigate to Home Page
        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(PaymentProcessingActivity.this, HomePageActivity2.class);
            startActivity(homeIntent);
            finish();
        });
    }

    private void displayChargingDetails() {
        Random random = new Random();

        // Simulated values
        int energyUsed = random.nextInt(40) + 10; // 10 to 50 kWh
        int hours = random.nextInt(5) + 1;
        int minutes = random.nextInt(60);
        String chargerId = "CHARGER PORT " + (random.nextInt(6) + 1);

        // Update UI
        chargerIdText.setText(chargerId);
        usedEnergyText.setText(String.format(Locale.getDefault(), "Used Energy: %d kWh", energyUsed));
        chargingTimeText.setText(String.format(Locale.getDefault(), "Time: %dh %dm", hours, minutes));

        // Calculate total amount based on real pricing
        double pricePerKWh = getRealisticPrice(chargingLevel, connectorType, network);
        if (pricePerKWh > 0) {
            double totalAmount = energyUsed * pricePerKWh;
            totalAmountText.setText(String.format(Locale.getDefault(), "$%.2f", totalAmount));
        } else {
            chargerIdText.setText("Start Charging Now!!");
            usedEnergyText.setText("Used Energy: 0 kWh");
            chargingTimeText.setText("Time: 0h 0m");
            totalAmountText.setText("Payment is Not Available!!");
        }
    }

    private double getRealisticPrice(String level, String connector, String network) {
        if (level == null || connector == null || network == null) return 0.0;

        // Realistic pricing based on your DOC
        if (level.equals("Level 1")) {
            if (connector.equals("Type1")) {
                switch (network) {
                    case "ChargePoint": return 0.12;
                    case "EVgo": return 0.13;
                    case "Electrify America": return 0.14;
                    case "Tesla": return 0.12;
                }
            } else if (connector.equals("Type2")) {
                switch (network) {
                    case "ChargePoint": return 0.13;
                    case "EVgo": return 0.14;
                    case "Electrify America": return 0.15;
                    case "Tesla": return 0.13;
                }
            } else if (connector.equals("CCS") || connector.equals("CHAdeMO")) {
                switch (network) {
                    case "ChargePoint": return 0.14;
                    case "EVgo": return 0.15;
                    case "Electrify America": return 0.16;
                    case "Tesla": return 0.14;
                }
            }
        }

        if (level.equals("Level 2")) {
            if (connector.equals("Type1")) {
                switch (network) {
                    case "ChargePoint": return 0.24;
                    case "EVgo": return 0.27;
                    case "Electrify America": return 0.30;
                    case "Tesla": return 0.25;
                }
            } else if (connector.equals("Type2")) {
                switch (network) {
                    case "ChargePoint": return 0.25;
                    case "EVgo": return 0.30;
                    case "Electrify America": return 0.35;
                    case "Tesla": return 0.28;
                }
            } else if (connector.equals("CCS") || connector.equals("CHAdeMO")) {
                switch (network) {
                    case "ChargePoint": return 0.27;
                    case "EVgo": return 0.32;
                    case "Electrify America": return 0.36;
                    case "Tesla": return 0.29;
                }
            }
        }

        if (level.equals("DC Fast")) {
            if (connector.equals("CCS")) {
                switch (network) {
                    case "ChargePoint": return 0.42;
                    case "EVgo": return 0.45;
                    case "Electrify America": return 0.48;
                    case "Tesla": return 0.40;
                }
            } else if (connector.equals("CHAdeMO")) {
                switch (network) {
                    case "ChargePoint": return 0.43;
                    case "EVgo": return 0.46;
                    case "Electrify America": return 0.47;
                    case "Tesla": return 0.44;
                }
            }
        }

        return 0.0; // Default if no match found
    }

    private void showFeedbackPopup(boolean isPositive) {
        String[] responses = isPositive ? new String[]{
                "Thanks for your feedback! We appreciate it!",
                "Awesome! We're glad you liked it!",
                "Glad you found this helpful!",
                "✅ Great! Thanks for the thumbs up!"
        } : new String[]{
                "Thanks for your feedback! We'll work on improving.",
                "Sorry to hear that! What could we do better?",
                "Oops! Didn’t meet your expectations?",
                "Oh no! We’ll try to do better next time."
        };

        Toast.makeText(this, getRandomMessage(responses), Toast.LENGTH_SHORT).show();
    }

    private String getRandomMessage(String[] messages) {
        return messages[new Random().nextInt(messages.length)];
    }
}
