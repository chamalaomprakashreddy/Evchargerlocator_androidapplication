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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String currentUserId;
    private DatabaseReference messagesRef;
    private Context context;

    public ChatAdapter(List<Message> messages, String currentUserId, Context context) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.context = context;
        this.messagesRef = FirebaseDatabase.getInstance().getReference("messages");
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
        holder.messageTime.setText(message.getFormattedTime());

        // ✅ Handle Sent/Received Messages & Read Receipts
        if (message.getSenderId().equals(currentUserId)) {
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_sent);
            holder.readReceipt.setVisibility(View.VISIBLE);
            holder.readReceipt.setImageResource(message.isRead() ? R.drawable.ic_double_tick : R.drawable.ic_single_tick);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_received);
            holder.readReceipt.setVisibility(View.GONE);
        }

        // ✅ Show reactions
        if (message.getReaction() != null && !message.getReaction().isEmpty()) {
            holder.reactionIcon.setVisibility(View.VISIBLE);
            holder.reactionIcon.setImageResource(getReactionDrawable(message.getReaction()));
        } else {
            holder.reactionIcon.setVisibility(View.GONE);
        }

        // ✅ Enable Long Press for Edit, Delete, and Reactions
        holder.itemView.setOnLongClickListener(v -> {
            showMessageOptions(holder, message);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    // ✅ Swipe-to-Delete Method
    public void removeItem(int position) {
        if (position >= 0 && position < messages.size()) {
            Message message = messages.get(position);
            messagesRef.child(message.getMessageId()).removeValue();
            messages.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ✅ Show Message Options (Edit, Delete, Reactions)
    private void showMessageOptions(MessageViewHolder holder, Message message) {
        PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.messageText);
        popup.getMenuInflater().inflate(R.menu.message_options_menu, popup.getMenu());

        // Add Reaction Options
        popup.getMenu().add("React 👍");
        popup.getMenu().add("React ❤️");
        popup.getMenu().add("React 😂");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.startsWith("React")) {
                updateReaction(message, title.substring(6));
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

    // ✅ Edit Message Feature
    private void editMessage(Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Message");

        final EditText input = new EditText(context);
        input.setText(message.getMessage());
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                messagesRef.child(message.getMessageId()).child("message").setValue(newText);
                message.setMessage(newText);
                notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ✅ Update Reaction
    private void updateReaction(Message message, String reaction) {
        messagesRef.child(message.getMessageId()).child("reaction").setValue(reaction);
        message.setReaction(reaction);
        notifyDataSetChanged();
    }

    // ✅ Map reactions to drawable resources
    private int getReactionDrawable(String reaction) {
        switch (reaction) {
            case "👍": return R.drawable.ic_thumb_up;
            case "❤️": return R.drawable.ic_heart;
            case "😂": return R.drawable.ic_laugh;
            default: return 0;
        }
    }

    // ✅ ViewHolder Class
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        ImageView readReceipt, reactionIcon;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            readReceipt = itemView.findViewById(R.id.readReceipt);
            reactionIcon = itemView.findViewById(R.id.reactionIcon);
        }
    }
}
