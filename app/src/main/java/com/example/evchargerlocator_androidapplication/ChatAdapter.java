package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> messageList;
    private String currentUserId;
    private Context context;

    public ChatAdapter(List<Message> messageList, String currentUserId, Context context) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageTextView.setText(message.getMessage());

        // Align messages (Left for received, Right for sent)
        if (message.getSenderId().equals(currentUserId)) {
            holder.messageTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            holder.messageTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ✅ Add this method to allow Swipe-to-Delete functionality
    public void removeItem(int position) {
        if (position >= 0 && position < messageList.size()) {
            messageList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateList(List<Message> newMessages) {
        messageList = newMessages;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        ViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}
