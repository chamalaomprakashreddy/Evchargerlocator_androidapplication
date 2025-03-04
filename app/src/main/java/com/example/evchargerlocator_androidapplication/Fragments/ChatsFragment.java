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

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<User> userList;
    private DatabaseReference userChatsRef, usersRef;
    private FirebaseAuth auth;
    private String currentUserId;

    public ChatsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // ✅ Initialize UI Components
        recyclerView = view.findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList, requireContext(), this::openChatWithUser);
        recyclerView.setAdapter(usersAdapter);

        // ✅ Initialize Firebase
        auth = FirebaseAuth.getInstance();
        userChatsRef = FirebaseDatabase.getInstance().getReference("user_chats");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            loadChats();
        } else {
            Toast.makeText(getContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadChats() {
        userChatsRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "No chats available", Toast.LENGTH_SHORT).show();
                    usersAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatUserId = chatSnapshot.getKey();

                    usersRef.child(chatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String userId = userSnapshot.getKey();
                                String email = userSnapshot.child("email").getValue(String.class);
                                String fullName = userSnapshot.child("fullName").getValue(String.class);
                                String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                                String vehicle = userSnapshot.child("vehicle").getValue(String.class);

                                if (userId != null && email != null && fullName != null) {
                                    userList.add(new User(userId, email, fullName, phoneNumber, vehicle));
                                    usersAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ChatsFragment", "Error loading user data: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatsFragment", "Failed to load chats: " + error.getMessage());
            }
        });
    }

    // ✅ Open Chat with Selected User
    private void openChatWithUser(User user) {
        Intent intent = new Intent(requireContext(), MessageActivity.class);
        intent.putExtra("receiverUserId", user.getId());
        intent.putExtra("receiverUserName", user.getFullName());
        startActivity(intent);
    }
}
