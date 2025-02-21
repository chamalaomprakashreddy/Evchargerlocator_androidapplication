package com.example.evchargerlocator_androidapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String senderId;
    private String message;
    private long timestamp;
    private String messageId;
    private boolean read;
    private String reaction;
    private String senderName; // Added field for storing sender's name

    // Default constructor required for Firebase
    public Message() {}

    public Message(String senderId, String message, long timestamp, String messageId, boolean read, String reaction) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.messageId = messageId;
        this.read = read;
        this.reaction = reaction;
        this.senderName = null;  // Initially null; will be set when fetching the username
    }

    // ✅ Getters and Setters

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // ✅ Method to format timestamp into human-readable time
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
