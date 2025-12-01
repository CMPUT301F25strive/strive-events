package com.example.eventlottery.data;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventlottery.model.NotificationLogEntry;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Repository facade for reading and updating notification logs from Firestore.
 */
public class NotificationRepository {

    private final FirebaseFirestore firestore;
    private final MutableLiveData<List<NotificationLogEntry>> notificationsLiveData =
            new MutableLiveData<>(Collections.emptyList());
    private ListenerRegistration listenerRegistration;

    public NotificationRepository() {
        firestore = FirebaseFirestore.getInstance();
        attachListener();
    }

    private void attachListener() {
        listenerRegistration = firestore.collection("notifications")
                .whereEqualTo("isSystem", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null || value == null) {
                            return;
                        }
                        List<NotificationLogEntry> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            NotificationLogEntry entry = parseDocument(doc);
                            if (entry != null) {
                                entries.add(entry);
                            }
                        }
                        entries.sort(Comparator.comparing(NotificationLogEntry::getSentAt).reversed());
                        notificationsLiveData.postValue(entries);
                    }
                });
    }

    private NotificationLogEntry parseDocument(@NonNull QueryDocumentSnapshot doc) {
        String senderId = doc.getString("senderId");
        String receiverId = doc.getString("receiverId");
        String message = doc.getString("message");
        if (TextUtils.isEmpty(senderId) || TextUtils.isEmpty(receiverId) || TextUtils.isEmpty(message)) {
            return null;
        }

        Object rawTimestamp = doc.get("timestamp");
        Date sentAt;
        if (rawTimestamp instanceof Timestamp) {
            sentAt = ((Timestamp) rawTimestamp).toDate();
        } else if (rawTimestamp instanceof Date) {
            sentAt = (Date) rawTimestamp;
        } else {
            sentAt = new Date(0);
        }

        boolean flagged = Boolean.TRUE.equals(doc.getBoolean("flagged"));
        boolean isSystem = Boolean.TRUE.equals(doc.getBoolean("isSystem"));

        return new NotificationLogEntry(
                doc.getId(),
                senderId,
                doc.getString("senderName"),
                receiverId,
                doc.getString("eventId"),
                doc.getString("eventTitle"),
                message,
                flagged,
                isSystem,
                sentAt
        );
    }

    public LiveData<List<NotificationLogEntry>> observeOrganizerNotifications() {
        return notificationsLiveData;
    }

    public void setFlagStatus(@NonNull String notificationId, boolean flagged) {
        firestore.collection("notifications")
                .document(notificationId)
                .update("flagged", flagged);
    }

    public void clear() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}
