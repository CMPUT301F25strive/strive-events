package com.example.eventlottery.model;
import android.content.Context;

/**
 * This is a mock of PushNotificationService for testing
 */
public class MockNotificationService {
    /**
     * Constructor for the MockNotificationService
     * @param context
     */
    public MockNotificationService(Context context) {}

    /**
     * Mock method implementation for send notification to avoid firebase usage
     * @param title: title of the notification
     * @param message: message of the notification
     */
    public void sendNotification(String title, String message) {
        System.out.println("[Mock Notification] " + title + " | " + message);
    }
}
