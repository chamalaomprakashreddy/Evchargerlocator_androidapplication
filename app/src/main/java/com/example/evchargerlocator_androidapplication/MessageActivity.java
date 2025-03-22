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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String editingMessageId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Log.d(TAG, "MessageActivity started");

        receiverUserId = getIntent().getStringExtra("receiverUserId");
        receiverUserName = getIntent().getStringExtra("receiverUserName");

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        if (receiverUserId == null || receiverUserName == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference resetUnreadRef = FirebaseDatabase.getInstance()
                .getReference("user_chats")
                .child(currentUserId)
                .child(receiverUserId);
        resetUnreadRef.child("unreadCount").setValue(0);

        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(getChatId(currentUserId, receiverUserId));
        userChatsRef = FirebaseDatabase.getInstance().getReference("user_chats");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        chatUserName = findViewById(R.id.chatUserName);
        typingIndicator = findViewById(R.id.typingIndicator);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        backButton = findViewById(R.id.backButton);
        btnClearChat = findViewById(R.id.btnClearChat);

        chatUserName.setText(receiverUserName);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, this);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        loadMessages();
        listenForTypingStatus();

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

        btnSend.setOnClickListener(v -> {
            if (editingMessageId != null) {
                updateMessage();
            } else {
                sendMessage();
            }
        });

        btnClearChat.setOnClickListener(v -> showClearChatDialog());
        backButton.setOnClickListener(v -> finish());
    }

    private String getChatId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void loadMessages() {
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                    if (!message.getSenderId().equals(currentUserId) && !message.isSeen()) {
                        markMessageAsSeen(message.getMessageId());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message updatedMessage = snapshot.getValue(Message.class);
                if (updatedMessage != null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).getMessageId().equals(updatedMessage.getMessageId())) {
                            messageList.set(i, updatedMessage);
                            messageAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void markMessageAsSeen(String messageId) {
        messagesRef.child(messageId).child("seen").setValue(true)
                .addOnSuccessListener(aVoid -> messageAdapter.notifyDataSetChanged());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (!messageText.isEmpty()) {
            String messageId = messagesRef.push().getKey();
            long timestamp = System.currentTimeMillis();

            Message message = new Message(
                    currentUserId,
                    receiverUserId,
                    messageText,
                    timestamp,
                    messageId,
                    false,
                    null,
                    null,
                    false
            );

            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        etMessage.setText("");
                        updateChatHistory(messageText, timestamp);

                        DatabaseReference senderChatRef = userChatsRef.child(currentUserId).child(receiverUserId);
                        DatabaseReference receiverChatRef = userChatsRef.child(receiverUserId).child(currentUserId);

                        senderChatRef.child("lastTimestamp").setValue(timestamp);
                        receiverChatRef.child("lastTimestamp").setValue(timestamp);

                        receiverChatRef.child("unreadCount").runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                Integer currentValue = currentData.getValue(Integer.class);
                                if (currentValue == null) {
                                    currentData.setValue(1);
                                } else {
                                    currentData.setValue(currentValue + 1);
                                }
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
                        });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void updateChatHistory(String lastMessage, long lastTimestamp) {
        Map<String, Object> chatUpdate = new HashMap<>();
        chatUpdate.put("lastMessage", lastMessage);
        chatUpdate.put("lastTimestamp", lastTimestamp);

        userChatsRef.child(currentUserId).child(receiverUserId).updateChildren(chatUpdate);
        userChatsRef.child(receiverUserId).child(currentUserId).updateChildren(chatUpdate);
    }

    private void updateMessage() {
        String updatedText = etMessage.getText().toString().trim();
        if (!updatedText.isEmpty() && editingMessageId != null) {
            messagesRef.child(editingMessageId).child("message").setValue(updatedText);
            messagesRef.child(editingMessageId).child("edited").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        editingMessageId = null;
                        etMessage.setText("");
                        btnSend.setImageResource(R.drawable.ic_send);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update message", Toast.LENGTH_SHORT).show());
        }
    }

    public void startEditingMessage(String messageId, String messageText) {
        editingMessageId = messageId;
        etMessage.setText(messageText);
        etMessage.setSelection(etMessage.getText().length());
        btnSend.setImageResource(R.drawable.ic_edit);
    }

    private void showClearChatDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> clearChat())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearChat() {
        messagesRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    messageList.clear();
                    messageAdapter.notifyDataSetChanged();
                    Toast.makeText(MessageActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(MessageActivity.this, "Failed to clear chat", Toast.LENGTH_SHORT).show());
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
