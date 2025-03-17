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
    private boolean seen;
    private String replyTo;
    private String reaction;
    private boolean edited; // ✅ Track if message was edited

    // ✅ Default Constructor (Firebase Requirement)
    public Message() {}

    // ✅ Constructor with Edited Field
    public Message(String senderId, String receiverId, String message, long timestamp, String messageId, boolean seen, String replyTo, String reaction, boolean edited) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.messageId = messageId;
        this.seen = seen;
        this.replyTo = replyTo;
        this.reaction = reaction;
        this.edited = edited;
    }

    // ✅ Getters & Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public String getReplyTo() { return replyTo; }
    public void setReplyTo(String replyTo) { this.replyTo = replyTo; }

    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    // ✅ Convert Timestamp to Readable Format
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
