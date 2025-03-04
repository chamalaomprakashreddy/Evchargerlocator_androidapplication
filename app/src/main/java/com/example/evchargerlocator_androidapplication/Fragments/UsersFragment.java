package com.example.evchargerlocator_androidapplication.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evchargerlocator_androidapplication.MessageActivity;
import com.example.evchargerlocator_androidapplication.R;
import com.example.evchargerlocator_androidapplication.User;
import com.example.evchargerlocator_androidapplication.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private String currentUserId;

    public UsersFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // ✅ Initialize UI Components
        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList, requireContext(), this::openChatWithUser);
        recyclerView.setAdapter(usersAdapter);

        // ✅ Initialize Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            loadUsers();
        } else {
            Toast.makeText(getContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                if (currentUserId == null) return; // ✅ Prevent crashes if user ID is null

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        // ✅ Extract user data
                        String userId = userSnapshot.getKey();
                        String email = userSnapshot.child("email").getValue(String.class);
                        String fullName = userSnapshot.child("fullName").getValue(String.class);
                        String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                        String vehicle = userSnapshot.child("vehicle").getValue(String.class);

                        if (userId != null && email != null && fullName != null) {
                            // ✅ Skip the currently logged-in user
                            if (!userId.equals(currentUserId)) {
                                userList.add(new User(userId, email, fullName, phoneNumber, vehicle));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("UsersFragment", "Error parsing user data", e);
                    }
                }

                if (userList.isEmpty()) {
                    Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                }

                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Open Chat with Specific User
    private void openChatWithUser(User user) {
        Intent intent = new Intent(requireContext(), MessageActivity.class);
        intent.putExtra("receiverUserId", user.getId());
        intent.putExtra("receiverUserName", user.getFullName());
        startActivity(intent);
    }
}
