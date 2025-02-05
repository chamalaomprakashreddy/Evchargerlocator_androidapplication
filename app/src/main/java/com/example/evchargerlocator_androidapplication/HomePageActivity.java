package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Define a map for menu item actions
        Map<Integer, Runnable> menuActions = new HashMap<>();



        menuActions.put(R.id.activity_trip, () -> {
            Toast.makeText(HomePageActivity.this, "trip ", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomePageActivity.this, TripPlannerActivity.class));
            // Add your logout logic (e.g., clear user session, navigate to login page)
        });


        menuActions.put(R.id.activity_chat, () -> {
            Toast.makeText(HomePageActivity.this, "Chat ", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomePageActivity.this, ChatActivity.class));
        });

        menuActions.put(R.id.activity_filter, () -> {
            Toast.makeText(HomePageActivity.this, "filter", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomePageActivity.this, FilterActivity.class));
        });

        menuActions.put(R.id.activity_user_profile, () -> {
            Toast.makeText(HomePageActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomePageActivity.this, UserProfileActivity.class));
        });

        // Set a listener for item selection in the BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Runnable action = menuActions.get(item.getItemId());
            if (action != null) {
                action.run();
                return true; // Return true to indicate the event is handled
            }
            return false; // Return false for unhandled menu items
        });
    }

}