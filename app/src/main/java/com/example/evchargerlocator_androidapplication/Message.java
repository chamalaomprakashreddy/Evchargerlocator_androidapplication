package com.example.evchargerlocator_androidapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String senderId;
    private String message;
    private long timestamp;  // Timestamp field to store the message send time
    private String messageId;  // Unique ID for each message

    // Default constructor (required for Firebase)
    public Message() {}

    // Constructor to initialize message object with all attributes
    public Message(String senderId, String message, long timestamp, String messageId) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.messageId = messageId;
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
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Method to format timestamp into a human-readable format (e.g., "10:30 AM")
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Format the time as "hh:mm AM/PM"
        return sdf.format(new Date(timestamp));  // Format timestamp to "hh:mm AM/PM"
    }
}
