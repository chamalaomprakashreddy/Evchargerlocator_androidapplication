package com.example.evchargerlocator_androidapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserId;

    // Constructor
    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
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
        holder.messageText.setText(message.getMessage());

        // Check if the message is from the current user
        if (message.getSenderId().equals(currentUserId)) {
            // Sent message: Use the "sent" bubble style
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_sent);
        } else {
            // Received message: Use the "received" bubble style
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_received);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder to represent individual messages
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}
