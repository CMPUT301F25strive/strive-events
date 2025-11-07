package com.example.eventlottery.model;
import android.content.Context;

public class MockNotificationService {
    public MockNotificationService(Context context) {

    }

    public void sendNotification(String title, String message) {
        // No Android code here — just simulate sending
        System.out.println("[Mock Notification] " + title + " | " + message);
    }
}
