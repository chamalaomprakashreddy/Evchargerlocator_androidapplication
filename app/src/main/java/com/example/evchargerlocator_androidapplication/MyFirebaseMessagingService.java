package com.example.evchargerlocator_androidapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "chat_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "📩 onMessageReceived from: " + remoteMessage.getFrom());

        if (!remoteMessage.getData().isEmpty()) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String senderId = remoteMessage.getData().get("senderId");
            String senderName = remoteMessage.getData().get("senderName");

            // Fallbacks if title/senderName are missing
            if (senderName == null || senderName.isEmpty()) {
                if (title != null && title.startsWith("Message from ")) {
                    senderName = title.replace("Message from ", "");
                } else {
                    senderName = "Chat User";
                }
            }

            if (senderId == null || senderId.isEmpty()) {
                Log.w(TAG, "❌ senderId is missing, cannot open specific chat.");
                return;
            }

            Log.d(TAG, "📦 Data Payload: " + remoteMessage.getData());
            showNotification(title, body, senderId, senderName);
        } else {
            Log.w(TAG, "⚠️ No data payload in FCM message.");
        }
    }


    private void showNotification(String title, String message, String senderId, String senderName) {
        if (senderId == null || senderId.isEmpty()) {
            Log.w(TAG, "⚠️ Missing senderId, cannot open specific chat.");
            return;
        }

        if (senderName == null || senderName.isEmpty()) {
            senderName = "Chat User"; // Fallback
        }

        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("receiverUserId", senderId);
        intent.putExtra("receiverUserName", senderName); // ✅ Send actual name
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                senderId.hashCode(), // Unique ID per chat
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for chat messages");
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "✅ Notification shown for: " + senderName);
    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔁 New FCM Token generated: " + token);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("deviceToken")
                    .setValue(token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Token saved to DB"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to save token", e));
        } else {
            Log.w(TAG, "⚠️ No user signed in to save FCM token.");
        }
    }
}
