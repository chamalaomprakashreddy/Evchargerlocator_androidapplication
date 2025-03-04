package com.example.evchargerlocator_androidapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> titleList = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position < fragmentList.size()) {
            return fragmentList.get(position);
        } else {
            throw new IndexOutOfBoundsException("Invalid fragment position");
        }
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        titleList.add(title);
    }

    public CharSequence getPageTitle(int position) {
        return position < titleList.size() ? titleList.get(position) : "";
    }
}
