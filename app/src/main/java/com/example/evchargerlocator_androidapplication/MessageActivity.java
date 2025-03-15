package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private TextView chatUserName, typingIndicator;
    private RecyclerView recyclerViewMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageView backButton, btnClearChat;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private DatabaseReference messagesRef, userChatsRef, userRef;
    private FirebaseAuth auth;
    private String currentUserId, receiverUserId, receiverUserName;
    private ChildEventListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Log.d(TAG, "MessageActivity started");

        // ✅ Retrieve Receiver Details
        receiverUserId = getIntent().getStringExtra("receiverUserId");
        receiverUserName = getIntent().getStringExtra("receiverUserName");

        if (receiverUserId == null || receiverUserName == null) {
            Log.e(TAG, "Receiver User ID or Name is null!");
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        } else {
            Log.e(TAG, "Firebase Authentication failed!");
            Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Firebase References (Fixed `getChatId()` issue)
        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(getChatId(currentUserId, receiverUserId));
        userChatsRef = FirebaseDatabase.getInstance().getReference("user_chats");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        // ✅ Initialize UI Components
        chatUserName = findViewById(R.id.chatUserName);
        typingIndicator = findViewById(R.id.typingIndicator);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        backButton = findViewById(R.id.backButton);
        btnClearChat = findViewById(R.id.btnClearChat); // ✅ Clear Chat Button

        chatUserName.setText(receiverUserName);

        // ✅ Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        // ✅ Load Messages in Real-Time
        loadMessages();

        // ✅ Listen for Typing Status
        listenForTypingStatus();

        // ✅ Detect Typing
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setTypingStatus(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ✅ Send Message
        btnSend.setOnClickListener(v -> sendMessage());

        // ✅ Clear Chat Button (New Feature)
        btnClearChat.setOnClickListener(v -> showClearChatDialog());

        // ✅ Back Button Click
        backButton.setOnClickListener(v -> finish());
    }

    // ✅ Fix: Generate Unique Chat ID based on User IDs
    private String getChatId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void loadMessages() {
        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);

                    if (!message.getSenderId().equals(currentUserId) && !message.isSeen()) {
                        messagesRef.child(message.getMessageId()).child("seen").setValue(true);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database Error: " + error.getMessage());
                Toast.makeText(MessageActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        };

        messagesRef.addChildEventListener(messageListener);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String messageId = messagesRef.push().getKey();
            long timestamp = System.currentTimeMillis();

            Message message = new Message(currentUserId, receiverUserId, messageText, timestamp, messageId, false);

            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        etMessage.setText("");
                        setTypingStatus(false);
                        Log.d(TAG, "Message sent successfully");
                        updateUserChats();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to send message: " + e.getMessage());
                        Toast.makeText(MessageActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateUserChats() {
        userChatsRef.child(currentUserId).child(receiverUserId).setValue(System.currentTimeMillis());
        userChatsRef.child(receiverUserId).child(currentUserId).setValue(System.currentTimeMillis());
    }

    // ✅ Show Confirmation Dialog for Clearing Chat
    private void showClearChatDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to clear this chat?")
                .setPositiveButton("Clear", (dialog, which) -> clearChat())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ✅ Clear Chat Messages from Firebase
    private void clearChat() {
        messagesRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    messageList.clear();
                    messageAdapter.notifyDataSetChanged();
                    Toast.makeText(MessageActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MessageActivity.this, "Failed to clear chat", Toast.LENGTH_SHORT).show();
                });
    }

    private void setTypingStatus(boolean isTyping) {
        if (currentUserId != null) {
            userRef.child(currentUserId).child("typing").setValue(isTyping);
        }
    }

    private void listenForTypingStatus() {
        userRef.child(receiverUserId).child("typing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isTyping = snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class);
                typingIndicator.setVisibility(isTyping ? View.VISIBLE : View.GONE);
                typingIndicator.setText(isTyping ? "typing..." : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setTypingStatus(false);
    }
}
