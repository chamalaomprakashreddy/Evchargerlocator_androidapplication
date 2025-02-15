package com.example.evchargerlocator_androidapplication;

public class Message {
    private String senderId;
    private String message;
    private long timestamp;

    // Default constructor (required for Firebase)
    public Message() {}

    // Constructor to initialize message object
    public Message(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
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
}
