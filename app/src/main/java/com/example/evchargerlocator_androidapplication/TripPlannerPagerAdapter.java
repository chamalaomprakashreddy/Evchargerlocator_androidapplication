package com.example.evchargerlocator_androidapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TripPlannerPagerAdapter extends FragmentStateAdapter {

    public TripPlannerPagerAdapter(@NonNull TripPlannerActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? new CreateTripFragment() : new MyTripsFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs (Create Trip & My Trips)
    }
}
