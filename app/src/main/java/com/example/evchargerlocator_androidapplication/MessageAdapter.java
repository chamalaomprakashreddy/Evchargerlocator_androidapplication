package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messageList;
    private String currentUserId;
    private Context context;

    public MessageAdapter(List<Message> messageList, String currentUserId, Context context) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        // ✅ Determine whether message is sent or received
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageText.setText(message.getMessage());

        // ✅ Format timestamp to readable format
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(message.getTimestamp());
        holder.timestampText.setText(formattedTime);

        // ✅ Display seen/unseen status for sent messages
        if (message.getSenderId().equals(currentUserId)) {
            if (message.isSeen()) {
                holder.seenStatus.setText("✔✔"); // Double check for seen
            } else {
                holder.seenStatus.setText("✔"); // Single check for sent but unseen
            }
        }

        // ✅ Long press to delete message (only sender can delete)
        if (message.getSenderId().equals(currentUserId)) {
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteMenu(v, message);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // ✅ Messages sent by the current user should be on the right
        return messageList.get(position).getSenderId().equals(currentUserId) ? 1 : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText, seenStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
            seenStatus = itemView.findViewById(R.id.seenStatus);
        }
    }

    // ✅ Show delete option for sender's messages
    private void showDeleteMenu(View view, Message message) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add("Delete");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Delete")) {
                deleteMessage(message);
            }
            return true;
        });

        popupMenu.show();
    }

    // ✅ Correctly delete message from Firebase
    private void deleteMessage(Message message) {
        String chatId = getChatId(currentUserId, message.getReceiverId());
        DatabaseReference messageRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatId)
                .child(message.getMessageId());

        messageRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    messageList.remove(message);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    // ✅ Generate Chat ID (Ensures consistency)
    private String getChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
