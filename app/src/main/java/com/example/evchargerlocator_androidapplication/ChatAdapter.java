package com.example.evchargerlocator_androidapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages;
    private List<Message> filteredMessages;  // List to store filtered messages for search
    private String currentUserId;
    private DatabaseReference messagesRef;

    // Constructor
    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.filteredMessages = messages;  // Initialize with all messages
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
        Message message = filteredMessages.get(position);

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

        // Set swipe-to-delete functionality (long press to delete message)
        holder.itemView.setOnLongClickListener(v -> {
            // Perform message deletion
            int positionToDelete = holder.getAdapterPosition();
            removeItem(positionToDelete);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredMessages.size();
    }

    // Remove an item from both Firebase and RecyclerView
    public void removeItem(int position) {
        Message message = filteredMessages.get(position);
        // Remove from Firebase
        messagesRef.child(message.getMessageId()).removeValue();  // Make sure to delete the message from Firebase

        // Remove from RecyclerView
        filteredMessages.remove(position);
        notifyItemRemoved(position);
    }

    // Method to update the filtered messages (for search functionality)
    public void updateMessages(List<Message> filteredMessages) {
        this.filteredMessages = filteredMessages;
        notifyDataSetChanged();  // Notify the adapter that the data has been updated
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
