package com.example.eventlottery.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.eventlottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.eventlottery.data.FirebaseProfileRepository;
import com.example.eventlottery.data.ProfileRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to send and listen for push notifications using Firestore.
 */
public class PushNotificationService {

    private final Context context;
    private final FirebaseFirestore firestore;
    private ProfileRepository profileRepository;
    private static final String CHANNEL_ID = "event_lottery_notifications";

    public PushNotificationService(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.profileRepository = new FirebaseProfileRepository();
        createNotificationChannel();
    }

    /**
     * This method sends a push notification by storing it in Firestore.
     * @param senderId the device ID of the sender
     * @param receiverId the device ID of the receiver
     * @param message the notification message
     * @param isSystem boolean to see if notification is from system or not
     */
    public void sendNotification(String senderId, String receiverId, String message, boolean isSystem) {
        sendNotification(senderId, receiverId, message, isSystem, null, null, null);
    }

    public void sendNotification(String senderId,
                                 String receiverId,
                                 String message,
                                 boolean isSystem,
                                 @Nullable String eventId,
                                 @Nullable String eventTitle,
                                 @Nullable String senderName) {
        Map<String, Object> data = new HashMap<>();
        data.put("senderId", senderId);
        data.put("receiverId", receiverId);
        data.put("message", message);
        data.put("delivered", false);
        data.put("timestamp", FieldValue.serverTimestamp());
        data.put("isSystem", isSystem);
        data.put("eventId", eventId);
        data.put("eventTitle", eventTitle);
        data.put("senderName", senderName);
        data.put("flagged", false);

        firestore.collection("notifications").add(data);
    }

    /**
     * This method listens for incoming notifications sent to this device.
     * @param deviceId the current device ID
     */
    public void listenForNotifications(String deviceId) {
        firestore.collection("notifications")
                .whereEqualTo("receiverId", deviceId)
                .whereEqualTo("delivered", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String message = doc.getString("message");
                        Profile profile = profileRepository.findUserById(deviceId);
                        boolean notificationsEnabled = profile == null || profile.getNotificationSettings();
                        if (notificationsEnabled) {
                            showNotification(message);
                            doc.getReference().update("delivered", true);
                        } else {
                            doc.getReference().update("delivered", true);
                        }
                    }
                });
    }

    /**
     * This method shows a local notification popup with the given message.
     * @param message the notification message
     */
    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Event Lottery")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return;

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * This method creates the notification channel used for sending notifications.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Lottery Notifications";
            String description = "Notifications for Event Lottery";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
