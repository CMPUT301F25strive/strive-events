package com.example.eventlottery.model;

import androidx.annotation.Nullable;

import java.util.Date;

/**
 * Immutable representation of a notification document for admin logs.
 */
public class NotificationLogEntry {

    private final String id;
    private final String senderId;
    private final String senderName;
    private final String receiverId;
    private final String eventId;
    private final String eventTitle;
    private final String message;
    private final boolean flagged;
    private final boolean system;
    private final Date sentAt;

    public NotificationLogEntry(String id,
                                String senderId,
                                @Nullable String senderName,
                                String receiverId,
                                @Nullable String eventId,
                                @Nullable String eventTitle,
                                String message,
                                boolean flagged,
                                boolean system,
                                Date sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.message = message;
        this.flagged = flagged;
        this.system = system;
        this.sentAt = sentAt;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    @Nullable
    public String getSenderName() {
        return senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    @Nullable
    public String getEventId() {
        return eventId;
    }

    @Nullable
    public String getEventTitle() {
        return eventTitle;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public boolean isSystem() {
        return system;
    }

    public Date getSentAt() {
        return sentAt;
    }
}
