package com.example.evchargerlocator_androidapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private String messageId;
    private boolean seen; // ✅ Added seen status

    // ✅ Default Constructor (Firebase Requirement)
    public Message() {
    }

    // ✅ Updated Constructor to include 'seen' status
    public Message(String senderId, String receiverId, String message, long timestamp, String messageId, boolean seen) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.messageId = messageId;
        this.seen = seen;
    }

    // ✅ Getters & Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
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

    public boolean isSeen() { // ✅ Getter for 'seen' status
        return seen;
    }

    public void setSeen(boolean seen) { // ✅ Setter for 'seen' status
        this.seen = seen;
    }

    // ✅ Convert Timestamp to Readable Date Format
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
