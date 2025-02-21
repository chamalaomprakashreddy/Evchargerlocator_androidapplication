package com.example.evchargerlocator_androidapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserId;
    private DatabaseReference messagesRef, usersRef;
    private Context context;
    private HashMap<String, String> userNamesCache;  // Cache usernames for better performance

    public ChatAdapter(List<Message> messages, String currentUserId, Context context) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.context = context;
        this.messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
        this.userNamesCache = new HashMap<>();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.messageText.setText(message.getMessage());
        holder.messageTime.setText(getFormattedTime(message.getTimestamp()));
        fetchUserName(message.getSenderId(), holder);

        // Sent/Received messages appearance
        if (message.getSenderId().equals(currentUserId)) {
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_sent);
            holder.readReceipt.setVisibility(View.VISIBLE);
            holder.readReceipt.setImageResource(message.isRead() ? R.drawable.ic_double_tick : R.drawable.ic_single_tick);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_received);
            holder.readReceipt.setVisibility(View.GONE);
        }

        // Display reactions
        if (message.getReaction() != null && !message.getReaction().isEmpty()) {
            holder.reactionIcon.setVisibility(View.VISIBLE);
            holder.reactionIcon.setImageResource(getReactionDrawable(message.getReaction()));
        } else {
            holder.reactionIcon.setVisibility(View.GONE);
        }

        // Long-press for message options (edit, delete, react)
        holder.itemView.setOnLongClickListener(v -> {
            showMessageOptions(holder, message);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ✅ Fetch user name from Firebase and cache it
    private void fetchUserName(String senderId, MessageViewHolder holder) {
        if (userNamesCache.containsKey(senderId)) {
            holder.userNameText.setText(userNamesCache.get(senderId));
        } else {
            usersRef.child(senderId).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fullName = snapshot.getValue(String.class);
                    if (fullName != null) {
                        userNamesCache.put(senderId, fullName);
                        holder.userNameText.setText(fullName);
                    } else {
                        holder.userNameText.setText("Unknown User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.userNameText.setText("Error Loading");
                }
            });
        }
    }

    // ✅ Display message options: react, edit, delete
    private void showMessageOptions(MessageViewHolder holder, Message message) {
        PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.messageText);
        popup.getMenuInflater().inflate(R.menu.message_options_menu, popup.getMenu());

        // Add reaction options
        popup.getMenu().add("React 👍");
        popup.getMenu().add("React ❤️");
        popup.getMenu().add("React 😂");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.startsWith("React")) {
                updateReaction(message, title.substring(6)); // Removes "React " and updates the emoji
                return true;
            } else if (item.getItemId() == R.id.edit_message) {
                editMessage(message);
                return true;
            } else if (item.getItemId() == R.id.delete_message) {
                removeItem(holder.getAdapterPosition());
                return true;
            }
            return false;
        });
        popup.show();
    }

    // ✅ Update the message reaction in Firebase
    private void updateReaction(Message message, String reaction) {
        messagesRef.child(message.getMessageId()).child("reaction").setValue(reaction);
        message.setReaction(reaction);
        notifyDataSetChanged();
    }

    // ✅ Edit message text
    // ✅ Method to edit message and update in Firebase
    private void editMessage(Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Message");

        // Create EditText for input
        final EditText input = new EditText(context);
        input.setText(message.getMessage());  // Pre-fill existing message
        builder.setView(input);

        // Handle the update
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty() && !newText.equals(message.getMessage())) {
                // ✅ Update message in Firebase
                messagesRef.child(message.getMessageId()).child("message").setValue(newText)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                message.setMessage(newText);  // Update locally
                                notifyDataSetChanged();  // Refresh RecyclerView
                            } else {
                                Toast.makeText(context, "Failed to update message", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Cancel Button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    // ✅ Swipe/Delete Functionality
    public void removeItem(int position) {
        if (position >= 0 && position < messages.size()) {
            Message message = messages.get(position);
            messagesRef.child(message.getMessageId()).removeValue();
            messages.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ✅ Convert timestamp to readable time
    private String getFormattedTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ✅ Map reaction string to drawable icon
    private int getReactionDrawable(String reaction) {
        switch (reaction) {
            case "👍": return R.drawable.ic_thumb_up;
            case "❤️": return R.drawable.ic_heart;
            case "😂": return R.drawable.ic_laugh;
            default: return 0;
        }
    }

    // ✅ ViewHolder class with username, message text, timestamp, read receipt, and reactions
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, userNameText;
        ImageView readReceipt, reactionIcon;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            userNameText = itemView.findViewById(R.id.userNameText);
            readReceipt = itemView.findViewById(R.id.readReceipt);
            reactionIcon = itemView.findViewById(R.id.reactionIcon);
        }
    }
}
