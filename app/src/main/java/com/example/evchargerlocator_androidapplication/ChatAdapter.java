package com.example.evchargerlocator_androidapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserId;
    private DatabaseReference messagesRef;

    // Constructor
    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.messagesRef = FirebaseDatabase.getInstance().getReference("messages");
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the view for each message
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        // Bind the message data to the ViewHolder
        Message message = messages.get(position);

        // Set the message text
        holder.messageText.setText(message.getMessage());

        // Set the timestamp next to the message
        holder.messageTime.setText(message.getFormattedTime());  // Format timestamp (hh:mm AM/PM)

        // Check if the message is from the current user
        if (message.getSenderId().equals(currentUserId)) {
            // Sent message: Use the "sent" bubble style
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_sent);
            holder.messageTime.setVisibility(View.GONE); // Optionally hide timestamp for sent messages
        } else {
            // Received message: Use the "received" bubble style
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_received);
            holder.messageTime.setVisibility(View.VISIBLE); // Show timestamp for received messages
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void removeItem(int position) {
        Message message = messages.get(position);
        // Remove from Firebase
        messagesRef.child(message.getMessageId()).removeValue();  // Make sure to delete the message from Firebase

        // Remove from RecyclerView
        messages.remove(position);
        notifyItemRemoved(position);
    }

    // ViewHolder to represent individual messages
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;  // TextView to display the timestamp

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);  // Initialize messageText
            messageTime = itemView.findViewById(R.id.messageTime);  // Initialize messageTime
        }
    }
}
