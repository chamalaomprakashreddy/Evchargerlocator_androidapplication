package com.example.evchargerlocator_androidapplication;

public class Message {
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private String messageId;

    // Default Constructor (Firebase Requirement)
    public Message() {
    }

    public Message(String senderId, String receiverId, String message, long timestamp, String messageId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.messageId = messageId;
    }

    // Getters & Setters
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
}
