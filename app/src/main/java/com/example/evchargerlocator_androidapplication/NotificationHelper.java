package com.example.evchargerlocator_androidapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "EV_Charger_App_Channel";
    private static final String PREFS_NAME = "NotificationsPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;

        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EV Charger Notifications";
            String description = "Notifications for EV Charger App";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Method to show a notification and save it to SharedPreferences
    @SuppressLint("MissingPermission")
    public void showNotification(String title, String message) {
        // Save notification to SharedPreferences
        saveNotification(title + ": " + message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)  // Ensure you have this drawable in your resources
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify((int) System.currentTimeMillis(), builder.build());
    }

    // Save notification to SharedPreferences
    private void saveNotification(String notification) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentNotifications = sharedPreferences.getString(KEY_NOTIFICATIONS, "");
        String newNotifications = currentNotifications + notification + "\n";

        // Save the updated notifications list
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NOTIFICATIONS, newNotifications);
        editor.apply();
    }

    // Get all saved notifications from SharedPreferences
    public String[] getNotifications() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String notifications = sharedPreferences.getString(KEY_NOTIFICATIONS, "");
        return notifications.split("\n");
    }
}
