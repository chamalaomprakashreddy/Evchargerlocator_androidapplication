package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.libraries.places.api.Places;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TripPlannerFragment extends Fragment {

    private static final String TAG = "TripPlannerFragment";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyD9kj3r7bl-InqThDFTljYBwKvUcRD5mKs";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView backArrow;

    public TripPlannerFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_planner, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        backArrow = view.findViewById(R.id.backArrow); // ✅ Make sure your fragment_trip_planner.xml has ImageView with id backArrow

        backArrow.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // ✅ FIX: use FragmentActivity for adapter constructor
        FragmentActivity activity = requireActivity();
        TripPlannerPagerAdapter pagerAdapter = new TripPlannerPagerAdapter(activity);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Create a Trip" : "My Trips");
        }).attach();

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), GOOGLE_MAPS_API_KEY);
            Log.d(TAG, "Google Places API initialized.");
        }

        return view;
    }
}
