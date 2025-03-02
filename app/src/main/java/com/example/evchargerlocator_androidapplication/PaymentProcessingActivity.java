package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class PaymentProcessingActivity extends AppCompatActivity {

    private TextView totalAmountText, usedEnergyText, chargingTimeText, chargerIdText;
    private ImageView thumbsUp, thumbsDown;
    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        totalAmountText = findViewById(R.id.totalAmount);
        usedEnergyText = findViewById(R.id.usedEnergyText);
        chargingTimeText = findViewById(R.id.chargingTimeText);
        chargerIdText = findViewById(R.id.chargerIdText); // Added chargerIdText
        thumbsUp = findViewById(R.id.thumbsUp);
        thumbsDown = findViewById(R.id.thumbsDown);
        homeButton = findViewById(R.id.homeButton);

        // Simulate payment processing
        new Handler().postDelayed(this::displayChargingDetails, 3000);

        // Handle thumbs up feedback
        thumbsUp.setOnClickListener(v -> showFeedbackPopup(true));

        // Handle thumbs down feedback
        thumbsDown.setOnClickListener(v -> showFeedbackPopup(false));

        // Navigate to HomeActivity when Home button is clicked
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentProcessingActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        });
    }

    private void displayChargingDetails() {
        Random random = new Random();

        // Generate random charging time (1 to 5 hours)
        int hours = random.nextInt(5) + 1;
        int minutes = random.nextInt(60);
        double totalTime = hours + (minutes / 60.0);

        // Generate random energy used (10-50 kWh)
        int energyUsed = random.nextInt(40) + 10;

        // Generate random charger ID and pricing type
        String chargerId = getRandomChargerId(random);
        double pricePerHour = getChargerPrice(chargerId); // Get the price based on charger type

        // Calculate total amount based on the price per hour
        double totalAmount = totalTime * pricePerHour;

        // Update UI with generated values
        chargerIdText.setText(chargerId); // Display random charger ID
        chargingTimeText.setText(String.format("Time: %dh %dm", hours, minutes));
        usedEnergyText.setText(String.format("Used Energy: %d kWh", energyUsed));
        totalAmountText.setText(String.format("$%.2f", totalAmount));
    }

    private String getRandomChargerId(Random random) {
        String[] chargerTypes = {"Level 1", "Level 2", "DC Fast"};
        int randomIndex = random.nextInt(chargerTypes.length);
        return "CHARGER #" + (randomIndex + 1) + " - " + chargerTypes[randomIndex];
    }

    private double getChargerPrice(String chargerId) {
        // Price adjustments based on charger type
        if (chargerId.contains("DC Fast")) {
            return 7.50; // Triple the regular price of $2.50
        } else if (chargerId.contains("Level 1")) {
            return 5.00; // Double the regular price of $2.50
        } else {
            return 2.50; // Regular price
        }
    }

    private void showFeedbackPopup(boolean isPositive) {
        String[] responses = isPositive ? new String[]{
                "Thanks for your feedback! We appreciate it!",
                "Awesome! We're glad you liked it! 😊",
                "Glad you found this helpful! Want to see more like this?",
                "✅ Great! Thanks for the thumbs up!"
        } : new String[]{
                "Thanks for your feedback! We'll work on improving.",
                "Sorry to hear that! What could we do better?",
                "Oops! Didn’t meet your expectations? Let us know why!",
                "Oh no! We’ll try to do better next time. 😞"
        };

        Toast.makeText(this, getRandomMessage(responses), Toast.LENGTH_SHORT).show();
    }

    private String getRandomMessage(String[] messages) {
        return messages[new Random().nextInt(messages.length)];
    }
}
