package com.example.evchargerlocator_androidapplication.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.evchargerlocator_androidapplication.MainActivity;
import com.example.evchargerlocator_androidapplication.R;
import com.example.evchargerlocator_androidapplication.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName, profileEmail, profilePhone, profileVehicle;
    private Button btnLogout;
    private DatabaseReference userRef;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private String userId;
    private Uri imageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // ✅ Initialize UI Elements
        profileImage = view.findViewById(R.id.profileImage);
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profilePhone = view.findViewById(R.id.profilePhone);
        profileVehicle = view.findViewById(R.id.profileVehicle);
        btnLogout = view.findViewById(R.id.btnLogout);

        // ✅ Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            storageReference = FirebaseStorage.getInstance().getReference("profile_images");
            loadUserProfile();
        } else {
            Toast.makeText(getContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
        }

        // ✅ Upload Profile Image on Click
        profileImage.setOnClickListener(v -> openImagePicker());

        // ✅ Logout Functionality
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        return view;
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        profileName.setText(user.getFullName());
                        profileEmail.setText(user.getEmail());
                        profilePhone.setText(user.getPhoneNumber());
                        profileVehicle.setText(user.getVehicle());

                        // ✅ Load Profile Image from Firebase
                        String imageUrl = snapshot.child("profileImage").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext()).load(imageUrl).into(profileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Open Image Picker
    private void openImagePicker() {
        ImagePicker.Companion.with(this)
                .cropSquare() // Crop Image to Square
                .compress(512) // Compress Image
                .maxResultSize(512, 512) // Set Max Size
                .start();
    }

    // ✅ Handle Selected Image
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            uploadImage();
        } else {
            Toast.makeText(getContext(), "Image selection failed!", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Upload Image to Firebase Storage
    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child(userId + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // ✅ Save Image URL to Firebase Database
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("profileImage", uri.toString());
                        userRef.updateChildren(updates);

                        Glide.with(requireContext()).load(uri.toString()).into(profileImage);
                        Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed!", Toast.LENGTH_SHORT).show());
        }
    }
}
