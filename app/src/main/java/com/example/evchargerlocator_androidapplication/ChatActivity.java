package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference messagesRef;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText etMessage;
    private ImageButton btnSend;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase and views
        database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Listen for incoming messages
        messagesRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String previousChildName) {
                Message message = dataSnapshot.getValue(Message.class);

                // Add the message to the list
                messageList.add(message);

                // Notify the adapter that a new item has been added
                chatAdapter.notifyItemInserted(messageList.size() - 1);

                // Scroll to the last message
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onChildChanged(com.google.firebase.database.DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(com.google.firebase.database.DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(com.google.firebase.database.DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                // Handle errors (e.g., show a message to the user)
                Toast.makeText(ChatActivity.this, "Error receiving messages: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up button click listener for sending messages
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        // Get the message text from the EditText
        String messageText = etMessage.getText().toString().trim();

        // Check if the message is not empty
        if (!messageText.isEmpty()) {
            // Disable the send button to prevent duplicate messages
            btnSend.setEnabled(false);

            // Show a Toast or progress indicator to inform the user
            Toast.makeText(this, "Sending message...", Toast.LENGTH_SHORT).show();

            // Get the current timestamp
            long timestamp = System.currentTimeMillis();

            // Create a new Message object
            Message message = new Message(currentUserId, messageText, timestamp);

            // Push the message to Firebase Realtime Database
            messagesRef.push().setValue(message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Reset the EditText after sending the message
                            etMessage.setText("");
                            hideKeyboard();  // Hide the keyboard after sending the message
                        } else {
                            // Show an error message if sending fails
                            Toast.makeText(ChatActivity.this, "Failed to send message. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        // Re-enable the send button
                        btnSend.setEnabled(true);
                    });
        } else {
            // Show a message if the user tries to send an empty message
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to hide the keyboard after sending the message
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        }
    }

    // Scroll RecyclerView to show the latest message
    @Override
    public void onResume() {
        super.onResume();
        recyclerView.scrollToPosition(messageList.size() - 1); // Ensure we are scrolled to the latest message
    }
}
