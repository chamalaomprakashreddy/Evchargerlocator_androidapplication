package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // ✅ Handle item click
        holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user));

        // ✅ Show "Online" or Last Seen Time
        if (user.getLastSeen() != null) {
            if (user.getLastSeen().equals("Online")) {
                holder.userStatus.setText("Online");
                holder.userStatus.setTextColor(Color.GREEN);
            } else {
                try {
                    long lastSeenTimestamp = Long.parseLong(user.getLastSeen());
                    String lastSeenTime = new SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault()).format(new Date(lastSeenTimestamp));
                    holder.userStatus.setText("Last seen: " + lastSeenTime);
                    holder.userStatus.setTextColor(Color.GRAY);
                } catch (NumberFormatException e) {
                    holder.userStatus.setText("Last seen: Unknown");
                    holder.userStatus.setTextColor(Color.GRAY);
                }
            }
        } else {
            holder.userStatus.setText("Last seen: Unknown");
            holder.userStatus.setTextColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.usernameText);
            userStatus = itemView.findViewById(R.id.userStatusText); // Make sure this exists in `user_item.xml`
        }
    }
}
