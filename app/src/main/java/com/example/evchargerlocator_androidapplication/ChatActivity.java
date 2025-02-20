package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference messagesRef;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private List<Message> filteredList; // List for search filtering
    private EditText searchMessageEditText, etMessage;
    private ImageButton btnSend;
    private String currentUserId;
    private TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerViewChat);
        searchMessageEditText = findViewById(R.id.searchMessageEditText);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        backButton = findViewById(R.id.backArrowText);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        filteredList = new ArrayList<>();
        chatAdapter = new ChatAdapter(filteredList, currentUserId, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Set Click Listener for Back Button
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });

        // Enable Swipe to Delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(chatAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        Log.d("ChatActivity", "Firebase Database Reference: " + messagesRef.toString());

        // Listen for messages from Firebase
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    if (!message.isRead() && !message.getSenderId().equals(currentUserId)) {
                        messagesRef.child(message.getMessageId()).child("read").setValue(true); // Mark as read
                    }
                    messageList.add(message);
                    filterMessages(searchMessageEditText.getText().toString()); // Filter messages dynamically
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message updatedMessage = snapshot.getValue(Message.class);
                if (updatedMessage != null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).getMessageId().equals(updatedMessage.getMessageId())) {
                            messageList.set(i, updatedMessage);
                            filterMessages(searchMessageEditText.getText().toString());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Message removedMessage = snapshot.getValue(Message.class);
                if (removedMessage != null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).getMessageId().equals(removedMessage.getMessageId())) {
                            messageList.remove(i);
                            filterMessages(searchMessageEditText.getText().toString());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatActivity", "Database Error: " + error.getMessage());
            }
        });

        // ✅ Search Functionality
        searchMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterMessages(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // ✅ Send Button Click Listener
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (!messageText.isEmpty()) {
            btnSend.setEnabled(false);

            String messageId = messagesRef.push().getKey();
            long timestamp = System.currentTimeMillis();
            Message message = new Message(currentUserId, messageText, timestamp, messageId, false, "");

            messagesRef.child(messageId).setValue(message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("ChatActivity", "Message sent successfully: " + messageText);
                            etMessage.setText("");
                            hideKeyboard();
                        } else {
                            Log.e("ChatActivity", "Failed to send message");
                            Toast.makeText(ChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                        }
                        btnSend.setEnabled(true);
                    });
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        }
    }

    private void filterMessages(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(messageList);
        } else {
            for (Message message : messageList) {
                if (message.getMessage().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(message);
                }
            }
        }
        chatAdapter.notifyDataSetChanged();
    }
}
