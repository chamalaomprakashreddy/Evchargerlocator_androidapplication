package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Faqs extends AppCompatActivity {

    private RecyclerView faqRecyclerView;
    private FaqAdapter faqAdapter;
    private List<FaqItem> faqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faqs);

        // Back arrow functionality
        TextView backArrow = findViewById(R.id.backArrowText);
        backArrow.setOnClickListener(v -> finish());

        // RecyclerView setup
        faqRecyclerView = findViewById(R.id.faqRecyclerView);
        faqRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize FAQ data
        faqList = new ArrayList<>();
        faqList.add(new FaqItem("What is this app?", "This app helps you locate EV chargers nearby."));
        faqList.add(new FaqItem("How do I make a payment?", "You can pay using credit/debit card or mobile wallets."));
        faqList.add(new FaqItem("Is the service free?", "Depends on the provider, networks and connects that you choose."));
        faqList.add(new FaqItem("How do I find a charger?", "You can search for nearby chargers using the map feature in the app."));
        faqList.add(new FaqItem("Do I need an account to use the app?", "Yes, you need to create an account to access all features."));
        faqList.add(new FaqItem("What types of chargers are supported?", "The app supports Level 1, Level 2, and DC Fast Chargers."));
        faqList.add(new FaqItem("Can I reserve a charger?", "Some charging stations allow reservations, check the app for availability."));
        faqList.add(new FaqItem("Is there a way to filter chargers by speed?", "Yes, you can use the filter option to find chargers based on speed and connector type."));
        faqList.add(new FaqItem("Are there any membership plans?", "Sorry about that, we don't offer any membership plans."));
        faqList.add(new FaqItem("How do I get directions to a charger?", "You can get directions to a selected charger through Google Maps integration in the app."));
        faqList.add(new FaqItem("Can I see the charging status of a station?", "Yes, the app provides real-time information on charger availability and status."));
        faqList.add(new FaqItem("How do I change my user details?", "You can update your user details through the 'user profile' section in the app."));
        faqList.add(new FaqItem("How do I contact support?", "You can contact support through the 'Chat' section in the app or email us at support@evchargerapp.com."));
        faqList.add(new FaqItem("What should I do if the app crashes?", "Try restarting the app. If the issue persists, please contact customer support."));
        faqList.add(new FaqItem("Can I see the cost of charging?", "Yes, the app displays the cost in station details."));
        faqList.add(new FaqItem("How do I update the app?", "You can update the app through the Google Play Store depending on your device."));

        // Setting up adapter
        faqAdapter = new FaqAdapter(faqList);
        faqRecyclerView.setAdapter(faqAdapter);
    }
}
