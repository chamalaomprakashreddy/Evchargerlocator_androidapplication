package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.evchargerlocator_androidapplication.Fragments.ChatsFragment;
import com.example.evchargerlocator_androidapplication.Fragments.UsersFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TextView backArrowText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.d(TAG, "ChatActivity started");

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        backArrowText = findViewById(R.id.backArrowText);

        try {
            viewPagerAdapter = new ViewPagerAdapter(this);
            viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
            viewPagerAdapter.addFragment(new UsersFragment(), "Users");
            //viewPagerAdapter.addFragment(new ProfileFragment(), "Profile"); // ✅ Add Profile Tab

            viewPager.setAdapter(viewPagerAdapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                    tab.setText(viewPagerAdapter.getPageTitle(position))
            ).attach();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing ViewPagerAdapter: " + e.getMessage());
        }

        backArrowText.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked - Closing ChatActivity");
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ChatActivity Destroyed");
    }
}
