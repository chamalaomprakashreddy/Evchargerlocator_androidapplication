package com.example.evchargerlocator_androidapplication.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evchargerlocator_androidapplication.MessageActivity;
import com.example.evchargerlocator_androidapplication.R;
import com.example.evchargerlocator_androidapplication.User;
import com.example.evchargerlocator_androidapplication.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerViewChats;
    private UsersAdapter usersAdapter;
    private List<User> userList;
    private DatabaseReference userChatsRef, usersRef;
    private String currentUserId;

    private static final String TAG = "ChatsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList, getContext(), this::openChat);
        recyclerViewChats.setAdapter(usersAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId != null) {
            userChatsRef = FirebaseDatabase.getInstance().getReference("user_chats").child(currentUserId);
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            loadChatUsers();
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadChatUsers() {
        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                List<User> tempUserList = new ArrayList<>();
                int totalUsers = (int) snapshot.getChildrenCount();
                if (totalUsers == 0) {
                    usersAdapter.notifyDataSetChanged();
                    return;
                }

                final int[] loadedCount = {0};

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userId = dataSnapshot.getKey();
                    Long tsValue = dataSnapshot.child("lastTimestamp").getValue(Long.class);
                    long lastMessageTimestamp = tsValue != null ? tsValue : 0L;

                    Integer unreadVal = dataSnapshot.child("unreadCount").getValue(Integer.class);
                    int unreadCount = unreadVal != null ? unreadVal : 0;

                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String fullName = userSnapshot.child("fullName").getValue(String.class);
                                String status = userSnapshot.child("status").getValue(String.class);

                                fullName = (fullName != null) ? fullName : "Unknown";
                                String displayStatus = (status != null && status.equals("Online")) ? "Online" : "Offline";

                                User user = new User(userId, fullName, displayStatus, lastMessageTimestamp, unreadCount);
                                tempUserList.add(user);
                            }

                            loadedCount[0]++;
                            if (loadedCount[0] == totalUsers) {
                                Collections.sort(tempUserList, (u1, u2) ->
                                        Long.compare(u2.getLastMessageTimestamp(), u1.getLastMessageTimestamp()));

                                userList.clear();
                                userList.addAll(tempUserList);
                                usersAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loadedCount[0]++;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading chat users: " + error.getMessage());
            }
        });
    }


    private void openChat(User user) {
        if (user == null || user.getId() == null) {
            Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), MessageActivity.class);
        intent.putExtra("receiverUserId", user.getId());
        intent.putExtra("receiverUserName", user.getFullName());
        startActivity(intent);
    }
}
