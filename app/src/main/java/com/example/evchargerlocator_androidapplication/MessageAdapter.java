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
    private DatabaseReference messagesRef;

    public MessageAdapter(List<Message> messageList, String currentUserId, Context context) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.context = context;
        this.messagesRef = FirebaseDatabase.getInstance().getReference("chats");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
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

        // ✅ Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        holder.timestampText.setText(sdf.format(message.getTimestamp()));

        // ✅ Show Seen/Unseen Status
        if (message.getSenderId().equals(currentUserId)) {
            holder.seenStatus.setText(message.isSeen() ? "✔✔" : "✔");
        }

        // ✅ Show emoji reactions if available
        if (message.getReaction() != null && !message.getReaction().isEmpty()) {
            holder.reactionText.setText(message.getReaction());
            holder.reactionText.setVisibility(View.VISIBLE);
        } else {
            holder.reactionText.setVisibility(View.GONE);
        }

        // ✅ Long press to edit or delete (Only sender can edit)
        if (message.getSenderId().equals(currentUserId)) {
            holder.itemView.setOnLongClickListener(v -> {
                showEditOrDeleteMenu(v, message);
                return true;
            });
        }

        // ✅ Tap message to add emoji reaction
        holder.itemView.setOnClickListener(v -> showEmojiPicker(holder, message));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderId().equals(currentUserId) ? 1 : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText, seenStatus, reactionText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
            seenStatus = itemView.findViewById(R.id.seenStatus);
            reactionText = itemView.findViewById(R.id.reactionText); // ✅ Display reactions properly
        }
    }

    // ✅ Show Edit or Delete Options
    private void showEditOrDeleteMenu(View view, Message message) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
            editMessage(message);
            return true;
        });
        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
            deleteMessage(message);
            return true;
        });
        popupMenu.show();
    }

    // ✅ Edit Message (Calling Correct Method in MessageActivity)
    private void editMessage(Message message) {
        if (context instanceof MessageActivity) {
            ((MessageActivity) context).startEditingMessage(message.getMessageId(), message.getMessage());
        }
    }

    // ✅ Delete Message
    private void deleteMessage(Message message) {
        String chatId = getChatId(currentUserId, message.getReceiverId());
        DatabaseReference messageRef = messagesRef.child(chatId).child(message.getMessageId());

        messageRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    messageList.remove(message);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    // ✅ Emoji Picker for Messages
    private void showEmojiPicker(ViewHolder holder, Message message) {
        PopupMenu popupMenu = new PopupMenu(context, holder.itemView);
        popupMenu.getMenu().add("👍").setOnMenuItemClickListener(item -> {
            updateReaction(message, "👍");
            return true;
        });
        popupMenu.getMenu().add("❤️").setOnMenuItemClickListener(item -> {
            updateReaction(message, "❤️");
            return true;
        });
        popupMenu.getMenu().add("😂").setOnMenuItemClickListener(item -> {
            updateReaction(message, "😂");
            return true;
        });
        popupMenu.show();
    }

    // ✅ Update Reaction in Firebase and Ensure UI Update
    private void updateReaction(Message message, String emoji) {
        String chatId = getChatId(currentUserId, message.getReceiverId());
        DatabaseReference messageRef = messagesRef.child(chatId).child(message.getMessageId());

        messageRef.child("reaction").setValue(emoji)
                .addOnSuccessListener(aVoid -> {
                    message.setReaction(emoji); // ✅ Update local object
                    notifyDataSetChanged(); // ✅ Refresh UI
                    Toast.makeText(context, "Reaction added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to add reaction!", Toast.LENGTH_SHORT).show());
    }

    // ✅ Generate Chat ID
    private String getChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
