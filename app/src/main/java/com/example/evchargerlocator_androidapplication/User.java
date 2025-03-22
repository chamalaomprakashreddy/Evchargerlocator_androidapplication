package com.example.evchargerlocator_androidapplication;

public class User implements Comparable<User> {
    private String id;
    private String fullName;
    private String status;
    private long lastMessageTimestamp; // For sorting chats
    private int unreadCount;           // To track unread messages
    private String lastMessage;        // ✅ For last message preview

    public User() {
        // Required empty constructor for Firebase
    }

    // ✅ Full constructor with lastMessage
    public User(String id, String fullName, String status, long lastMessageTimestamp, int unreadCount, String lastMessage) {
        this.id = id;
        this.fullName = fullName != null ? fullName : "Unknown";
        this.status = status != null ? status : "Offline";
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage != null ? lastMessage : "";
    }

    // ✅ Optional: support old constructor for backward compatibility
    public User(String id, String fullName, String status, long lastMessageTimestamp, int unreadCount) {
        this(id, fullName, status, lastMessageTimestamp, unreadCount, "");
    }

    // Getters
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

    public String getLastMessage() {
        return lastMessage != null ? lastMessage : "";
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    // Sorting users based on the last message timestamp (latest first)
    @Override
    public int compareTo(User otherUser) {
        return Long.compare(otherUser.getLastMessageTimestamp(), this.lastMessageTimestamp);
    }
}
