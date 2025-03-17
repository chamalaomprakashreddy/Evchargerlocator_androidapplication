package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TripPlannerActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView backArrowText;

    private TripPlannerPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        backArrowText = findViewById(R.id.backArrowText);

        // Go back to the previous screen
        backArrowText.setOnClickListener(v -> finish());



        // ✅ Ensure ViewPager2 is NOT null before setting an adapter
        if (viewPager != null) {
            pagerAdapter = new TripPlannerPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0) {
                    tab.setText("Create a Trip");
                } else {
                    tab.setText("My Trips");
                }
            }).attach();
        }
    }
}
