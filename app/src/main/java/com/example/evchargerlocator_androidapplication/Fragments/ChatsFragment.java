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

import com.example.evchargerlocator_androidapplication.Message;
import com.example.evchargerlocator_androidapplication.MessageActivity;
import com.example.evchargerlocator_androidapplication.R;
import com.example.evchargerlocator_androidapplication.User;
import com.example.evchargerlocator_androidapplication.UsersAdapter;
import com.google.android.material.snackbar.Snackbar;
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
        usersAdapter = new UsersAdapter(userList, getContext(), this::openChat, this::deleteChat);
        recyclerViewChats.setAdapter(usersAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId != null) {
            userChatsRef = FirebaseDatabase.getInstance().getReference("user_chats").child(currentUserId);
            usersRef = FirebaseDatabase.getInstance().getReference("users");
            loadChatUsers();
            listenForIncomingMessages(); // ✅ In-app notifications
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

                    final long[] lastMessageTimestamp = {0};
                    final int[] unreadCount = {0};
                    final String[] lastMessage = {""};

                    Long tsValue = dataSnapshot.child("lastTimestamp").getValue(Long.class);
                    if (tsValue != null) lastMessageTimestamp[0] = tsValue;

                    Integer unreadVal = dataSnapshot.child("unreadCount").getValue(Integer.class);
                    if (unreadVal != null) unreadCount[0] = unreadVal;

                    String messageVal = dataSnapshot.child("lastMessage").getValue(String.class);
                    if (messageVal != null) lastMessage[0] = messageVal;

                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String fullName = userSnapshot.child("fullName").getValue(String.class);
                                String status = userSnapshot.child("status").getValue(String.class);

                                fullName = (fullName != null) ? fullName : "Unknown";
                                String displayStatus = (status != null && status.equals("Online")) ? "Online" : "Offline";

                                User user = new User(
                                        userId,
                                        fullName,
                                        displayStatus,
                                        lastMessageTimestamp[0],
                                        unreadCount[0],
                                        lastMessage[0]
                                );

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

    private void listenForIncomingMessages() {
        if (currentUserId == null) return;

        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("user_chats").child(currentUserId);

        userChatsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot chatSnapshot, @Nullable String previousChildName) {
                String otherUserId = chatSnapshot.getKey();
                if (otherUserId == null) return;

                String chatId = getChatId(currentUserId, otherUserId);

                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);
                chatRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot messageSnapshot, @Nullable String previousChildName) {
                        Message msg = messageSnapshot.getValue(Message.class);
                        if (msg == null) return;

                        if (msg.getReceiverId().equals(currentUserId) && !msg.isSeen()) {
                            showInAppNotification(msg.getSenderId(), msg.getMessage());
                        }
                    }

                    @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getChatId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }



    private void showInAppNotification(String senderId, String messageText) {
        usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String[] senderName = {snapshot.child("fullName").getValue(String.class)};
                if (senderName[0] == null) senderName[0] = "Someone";

                Snackbar.make(recyclerViewChats,
                        "📩 New message from " + senderName[0] + ": " + messageText,
                        Snackbar.LENGTH_LONG
                ).setAction("Open Chat", v -> openChat(new User(
                        senderId,
                        senderName[0],
                        "", // status
                        0, 0, "" // timestamp, unread, preview
                ))).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
    private void deleteChat(User user) {
        if (user == null || user.getId() == null) return;

        String otherUserId = user.getId();
        String chatId = getChatId(currentUserId, otherUserId);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        // 1. Remove user_chats for current user
        dbRef.child("user_chats").child(currentUserId).child(otherUserId).removeValue()
                .addOnCompleteListener(task1 -> {
                    // 2. Remove user_chats for other user
                    dbRef.child("user_chats").child(otherUserId).child(currentUserId).removeValue()
                            .addOnCompleteListener(task2 -> {
                                // 3. Remove shared chat history
                                dbRef.child("chats").child(chatId).removeValue()
                                        .addOnSuccessListener(unused ->
                                                Toast.makeText(getContext(), "Chat deleted for both users", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(getContext(), "Failed to delete messages", Toast.LENGTH_SHORT).show());
                            });
                });
    }

}
