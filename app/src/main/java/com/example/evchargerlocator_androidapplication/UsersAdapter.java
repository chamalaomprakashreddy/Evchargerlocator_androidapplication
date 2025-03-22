package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UsersAdapter(List<User> userList, Context context, OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.context = context;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getFullName());

        // ✅ Set last message preview
        holder.lastMessagePreview.setText(user.getLastMessage());

        // ✅ Display online/offline status
        if (user.isOnline()) {
            holder.userStatus.setText("Online");
            holder.userStatus.setTextColor(Color.GREEN);
        } else {
            holder.userStatus.setText("Offline");
            holder.userStatus.setTextColor(Color.GRAY);
        }

        // ✅ Show unread badge if applicable
        if (user.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(user.getUnreadCount()));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        // ✅ Handle chat click
        holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void sortUsersByRecentChats() {
        Collections.sort(userList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus, unreadBadge, lastMessagePreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.usernameText);
            userStatus = itemView.findViewById(R.id.userStatusText);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            lastMessagePreview = itemView.findViewById(R.id.lastMessagePreview); // ✅ Make sure it's in XML
        }
    }
}
