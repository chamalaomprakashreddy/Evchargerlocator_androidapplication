package com.example.evchargerlocator_androidapplication;


import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;

public class FirebaseAuthTokenProvider {

    private static final String TAG = "FirebaseAuthTokenProvider";
    private static String cachedToken;
    private static long tokenExpiryTime;

    public static String getAccessToken(Context context) {
        long currentTime = System.currentTimeMillis();

        // Check if token is still valid (Tokens are valid for 1 hour)
        if (cachedToken != null && currentTime < tokenExpiryTime) {
            return cachedToken;
        }

        try {
            // ✅ Load Firebase Service Account JSON
            InputStream serviceAccountStream = context.getAssets().open("service-account.json");

            // ✅ Get OAuth Token
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped("https://www.googleapis.com/auth/firebase.messaging");

            credentials.refreshIfExpired();
            cachedToken = credentials.getAccessToken().getTokenValue();

            // ✅ Set Expiry Time
            tokenExpiryTime = System.currentTimeMillis() + 3600 * 1000; // 1 hour validity

            Log.d(TAG, "✅ Fetched new Firebase OAuth Token");
            return cachedToken;
        } catch (IOException e) {
            Log.e(TAG, "❌ Failed to get Firebase OAuth Token", e);
            return null;
        }
    }
}
