package com.example.evchargerlocator_androidapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TripPlannerPagerAdapter extends FragmentStateAdapter {

    public TripPlannerPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? new CreateTripFragment() : new MyTripsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
