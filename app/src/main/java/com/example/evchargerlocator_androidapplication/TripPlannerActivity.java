package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.widget.TextView;

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

        // Bind views
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        backArrowText = findViewById(R.id.backArrowText);

        // Back button to return to previous screen
        backArrowText.setOnClickListener(v -> finish());

        // ✅ Set up ViewPager and Adapter
        pagerAdapter = new TripPlannerPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // ✅ Link tabs to ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Create a Trip");
            } else {
                tab.setText("My Trips");
            }
        }).attach();

        // ✅ If navigated from TripBadge or saved trip, switch to "My Trips" tab
        if (getIntent().getBooleanExtra("navigateToMyTrips", false)) {
            viewPager.setCurrentItem(1, false);  // 1 = My Trips tab
        }
    }
}
