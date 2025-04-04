package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends ComponentActivity {

    private RecyclerView notificationsRecyclerView;
    private NotificationsAdapter notificationsAdapter;
    private List<String> notificationList;
    private NotificationHelper notificationHelper;
    private static final String DEFAULT_NOTIFICATION_SHOWN = "default_notification_shown";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize Notification Helper
        notificationHelper = new NotificationHelper(this);

        // Back arrow functionality
        TextView backArrow = findViewById(R.id.backArrowText);
        backArrow.setOnClickListener(v -> finish());

        // RecyclerView setup
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize notification list and adapter
        notificationList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationsAdapter);


        // Show default notification if not shown already
        if (!hasDefaultNotificationBeenShown()) {
            showDefaultNotification();
            setDefaultNotificationShown();
        }

        // Load saved notifications
        loadNotifications();

        // Handle Intent to trigger notifications
        handleIntent(getIntent());
    }


    // Show default "Thank you for using the app" notification when the activity starts
    private void showDefaultNotification() {
        notificationHelper.showNotification("EV Charger App", "Thank you for using the EV Charger App!");
    }
    // Check if the default notification has been shown
    private boolean hasDefaultNotificationBeenShown() {
        return getSharedPreferences("notifications", MODE_PRIVATE)
                .getBoolean(DEFAULT_NOTIFICATION_SHOWN, false);
    }

    // Mark the default notification as shown
    private void setDefaultNotificationShown() {
        getSharedPreferences("notifications", MODE_PRIVATE)
                .edit()
                .putBoolean(DEFAULT_NOTIFICATION_SHOWN, true)
                .apply();
    }


    // Handle incoming intents to trigger notifications
    private void handleIntent(Intent intent) {
        String notificationType = intent.getStringExtra("notification_type");
        String message = intent.getStringExtra("message");

        // Check notification type and show appropriate notification
        if ("trip_confirmed".equals(notificationType)) {
            notificationHelper.showNotification("Trip Confirmed", message);
        }

        // Reload notifications after saving the new one
        loadNotifications();
    }

    // Load notifications from SharedPreferences
    private void loadNotifications() {
        String[] savedNotifications = notificationHelper.getNotifications();
        notificationList.clear();
        for (String notification : savedNotifications) {
            if (!notification.isEmpty()) {
                notificationList.add(notification);
            }
        }
        notificationsAdapter.notifyDataSetChanged();
    }
}
