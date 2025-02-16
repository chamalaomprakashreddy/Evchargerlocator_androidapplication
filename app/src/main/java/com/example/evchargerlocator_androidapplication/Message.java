package com.example.evchargerlocator_androidapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String senderId;
    private String message;
    private long timestamp;
    private String messageId;  // Unique message ID for Firebase

    // Default constructor (required for Firebase)
    public Message() {}

    // Constructor to initialize message object with an additional messageId
    public Message(String senderId, String message, long timestamp, String messageId) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.messageId = messageId;  // Set the unique message ID
    }

    // Getter and Setter methods
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
        return messageId;  // Getter for message ID
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;  // Setter for message ID
    }

    // Method to format timestamp into a human-readable format (e.g., "10:30 AM")
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Format the time as "hh:mm AM/PM"
        return sdf.format(new Date(timestamp));  // Format timestamp to "hh:mm AM/PM"
    }
}
