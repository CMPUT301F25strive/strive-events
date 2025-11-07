package com.example.eventlottery.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.eventlottery.R;

/**
 * Minimal helper for creating the notification channel and dispatching event invitations.
 * Encapsulating this logic keeps repository/service classes free from Android framework details.
 */
public class PushNotificationService {
    private final Context context;
    private static final String CHANNEL_ID = "event_channel_id";
    private static final int NOTIFICATION_ID = 1;


    /**
     * Builds a notification helper bound to the provided context.
     *
     * @param context Android context used to access system services
     */
    public PushNotificationService(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * creates a channel for the notifications
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Notifications";
            String description = "Notifications for event invitations";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Sends a notification summarizing the event invitation to the user.
     *
     * @param title   title for the notification
     * @param message message for the notification
     */
    public void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }
}
