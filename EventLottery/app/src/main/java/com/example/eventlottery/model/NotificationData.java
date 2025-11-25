package com.example.eventlottery.model;

import com.google.firebase.firestore.FieldValue;

/**
 * Represents a push notification stored in Firestore.
 */
public class NotificationData {
    public String senderId;
    public String receiverId;
    public String message;
    public boolean delivered;
    public Object timestamp;

    public NotificationData() {}

    /**
     * Constructor for a new notification object.
     * @param senderId the sender device ID
     * @param receiverId the receiver device ID
     * @param message the notification message
     */
    public NotificationData(String senderId, String receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.delivered = false;
        this.timestamp = FieldValue.serverTimestamp();
    }
}