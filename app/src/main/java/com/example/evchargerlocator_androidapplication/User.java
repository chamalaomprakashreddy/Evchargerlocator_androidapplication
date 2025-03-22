package com.example.evchargerlocator_androidapplication;

public class User implements Comparable<User> {
    private String id;
    private String fullName;
    private String status;
    private long lastMessageTimestamp; // For sorting chats
    private int unreadCount; // To track unread messages

    public User() {}

    public User(String id, String fullName, String status, long lastMessageTimestamp, int unreadCount) {
        this.id = id;
        this.fullName = fullName != null ? fullName : "Unknown"; // Default name if null
        this.status = status != null ? status : "Offline"; // Ensure it defaults to "Offline"
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unreadCount = unreadCount;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOnline() {
        return "Online".equals(status);
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    // Sorting users based on the last message timestamp (latest first)
    @Override
    public int compareTo(User otherUser) {
        return Long.compare(otherUser.getLastMessageTimestamp(), this.lastMessageTimestamp);
    }
}
