package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.github.dhaval2404.imagepicker.ImagePicker;
public class UserProfileActivity extends AppCompatActivity {

    private EditText fullName, phoneNumber, email, vehicle;
    private Button editButton, paymentButton;
    private ImageView profileImage;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef, onlineStatusRef;
    private StorageReference storageReference;
    private boolean isEditing = false; // Track edit mode
    private String userId;
    private Uri imageUri; // Store selected image URI

    // Registering ActivityResultLauncher for gallery
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    profileImage.setImageURI(uri);
                    uploadProfileImage(uri);
                }
            });

    // Registering ActivityResultLauncher for camera
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && imageUri != null) {
                    profileImage.setImageURI(imageUri);
                    uploadProfileImage(imageUri); // Upload to Firebase Storage
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Views
        TextView backArrowText = findViewById(R.id.backArrowText);
        fullName = findViewById(R.id.fullName);
        phoneNumber = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.email);
        vehicle = findViewById(R.id.vehicle);
        editButton = findViewById(R.id.editButton);
        paymentButton = findViewById(R.id.PaymentButton);
        profileImage = findViewById(R.id.profileImage);

        // Firebase Setup
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            onlineStatusRef = userRef.child("lastSeen");

            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(currentUser.getUid());
            showUserData();
            setUserOnline();  // ✅ Set user as online when activity is opened
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Back Arrow Functionality
        backArrowText.setOnClickListener(v -> finish());

        // Profile picture click event
        profileImage.setOnClickListener(v -> selectImageOption());
        editButton.setOnClickListener(v -> toggleEditProfile());
        paymentButton.setOnClickListener(v -> openPaymentActivity());
    }

    public void showUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fullName.setText(snapshot.child("fullName").getValue(String.class));
                    email.setText(snapshot.child("email").getValue(String.class));
                    phoneNumber.setText(snapshot.child("phoneNumber").getValue(String.class));
                    vehicle.setText(snapshot.child("vehicle").getValue(String.class));

                    // Load profile image if available
                    String profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(UserProfileActivity.this).load(profileImageUrl).into(profileImage);
                    }
                }
                setFieldsEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditProfile() {
        isEditing = !isEditing;

        if (isEditing) {
            setFieldsEnabled(true);
            editButton.setText("Save");
        } else {
            saveUserProfile();
            setFieldsEnabled(false);
            editButton.setText("Edit Profile");
        }
    }

    private void saveUserProfile() {
        String updatedFullName = fullName.getText().toString().trim();
        String updatedPhoneNumber = phoneNumber.getText().toString().trim();
        String updatedVehicle = vehicle.getText().toString().trim();

        if (updatedFullName.isEmpty() || updatedPhoneNumber.isEmpty() || updatedVehicle.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("fullName").setValue(updatedFullName);
        userRef.child("phoneNumber").setValue(updatedPhoneNumber);
        userRef.child("vehicle").setValue(updatedVehicle);

        Toast.makeText(UserProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void setFieldsEnabled(boolean enabled) {
        fullName.setEnabled(enabled);
        phoneNumber.setEnabled(enabled);
        vehicle.setEnabled(enabled);
    }

    private void openPaymentActivity() {
        startActivity(new Intent(UserProfileActivity.this, PaymentActivity.class));
    }

    /**
     * Prompts user to choose an image option.
     */
    private void selectImageOption() {
        String[] options = {"Take a Photo", "Remove Profile Picture"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openImagePicker(); // Open Image Picker instead of Camera
                    } else {
                        removeProfilePicture();
                    }
                }).show();
    }

    /**
     * Opens Image Picker to select and crop image.
     */
    private void openImagePicker() {
        ImagePicker.Companion.with(this)
                .cropSquare() // Crop Image to Square
                .compress(512) // Compress Image
                .maxResultSize(512, 512) // Set Max Size
                .start();
    }

    /**
     * Uploads image to Firebase Storage.
     */
    private void uploadProfileImage(Uri uri) {
        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot ->
                storageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    userRef.child("profileImage").setValue(downloadUri.toString());
                    Toast.makeText(UserProfileActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                })
        );
    }

    /**
     * Removes profile picture from Firebase.
     */
    private void removeProfilePicture() {
        userRef.child("profileImage").removeValue();
        profileImage.setImageResource(R.drawable.default_profile);
        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show();
    }

    // Handle Image Picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                imageUri = uri;
                profileImage.setImageURI(uri);
                uploadProfileImage(uri);
            }
        }
    }

    private void setUserOnline() {
        if (onlineStatusRef != null) {
            onlineStatusRef.setValue("Online");
        }
    }

    private void setUserOffline() {
        if (onlineStatusRef != null) {
            String lastSeenTime = String.valueOf(System.currentTimeMillis());
            onlineStatusRef.setValue(lastSeenTime);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserOffline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserOnline();
    }
}
