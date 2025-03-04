package com.example.evchargerlocator_androidapplication;

import android.os.Bundle;
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
    private TextView chatUserName;
    private RecyclerView recyclerViewMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageView backButton;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private DatabaseReference messagesRef;
    private FirebaseAuth auth;
    private String currentUserId, receiverUserId, receiverUserName;
    private ChildEventListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        receiverUserId = getIntent().getStringExtra("receiverUserId");
        receiverUserName = getIntent().getStringExtra("receiverUserName");

        if (receiverUserId == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(getChatId(currentUserId, receiverUserId));

        chatUserName = findViewById(R.id.chatUserName);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        backButton = findViewById(R.id.backButton);

        chatUserName.setText(receiverUserName);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, this);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        loadMessages();
        btnSend.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());
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
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        messagesRef.addChildEventListener(messageListener);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String messageId = messagesRef.push().getKey();
            long timestamp = System.currentTimeMillis();
            Message message = new Message(currentUserId, receiverUserId, messageText, timestamp, messageId);

            messagesRef.child(messageId).setValue(message);
            etMessage.setText("");
        }
    }

    private String getChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
