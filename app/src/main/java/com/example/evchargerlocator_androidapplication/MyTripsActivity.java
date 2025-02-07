package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyTripsActivity extends AppCompatActivity {

    private TextView backArrowText;
    private ListView tripsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_trips);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backArrowText = findViewById(R.id.backArrowText);
        tripsListView = findViewById(R.id.tripsListView);


        // Set up the back arrow functionality
        backArrowText.setOnClickListener(v -> finish()); // Go back to the previous screen



    }
}