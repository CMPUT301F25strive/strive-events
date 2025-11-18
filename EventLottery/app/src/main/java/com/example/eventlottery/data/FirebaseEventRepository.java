package com.example.eventlottery.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventlottery.model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import android.net.Uri;

public class FirebaseEventRepository implements EventRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference eventsRef;
    private final FirebaseStorage storage;

    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>(new ArrayList<>());

    public FirebaseEventRepository() {
        firestore = FirebaseFirestore.getInstance();
        eventsRef = firestore.collection("events");
        storage = FirebaseStorage.getInstance();

        // Start real-time listener
        listenForEvents();
    }

    // ============================================================
    // REAL-TIME FIRESTORE LISTENER
    // ============================================================
    private void listenForEvents() {
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {

                if (snapshots == null) return;

                List<Event> updated = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) {
                        // Ensure waitingList and attendeesList are non-null
                        if (event.getWaitingList() == null) event.setWaitingList(new ArrayList<>());
                        if (event.getAttendeesList() == null) event.setAttendeesList(new ArrayList<>());
                        updated.add(event);
                    }
                }

                eventsLiveData.postValue(updated);
            }
        });
    }

    // ============================================================
    // OBSERVE EVENTS
    // ============================================================
    @NonNull
    @Override
    public LiveData<List<Event>> observeEvents() {
        return eventsLiveData;
    }

    @Override
    public void refresh() {
        // Firestore is real-time, no manual refresh needed
    }

    // ============================================================
    // FIND EVENT BY ID
    // ============================================================
    @Override
    public Event findEventById(String id) {
        List<Event> list = eventsLiveData.getValue();
        if (list == null) return null;

        for (Event e : list) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    // ============================================================
    // UPDATE WAITING LIST
    // ============================================================
    @Override
    public void updateWaitingList(String eventID, List<String> waitingList) {
        eventsRef.document(eventID)
                .update("waitingList", waitingList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // ============================================================
    // DELETE EVENT
    // ============================================================
    @Override
    public void deleteEvent(String eventId) {
        eventsRef.document(eventId)
                .delete()
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // ============================================================
    // UPLOAD NEW EVENT
    // ============================================================
    public interface UploadCallback {
        void onComplete(boolean success, String message);
    }

    public void uploadEvent(Uri imageUri,
                            String title,
                            String description,
                            String location,
                            String date,
                            String time,
                            int maxParticipants,
                            Event.Tag tag,
                            @NonNull UploadCallback callback) {

        // Generate unique event ID
        String eventId = UUID.randomUUID().toString();

        if (imageUri != null) {
            StorageReference imageRef = storage.getReference()
                    .child("event_posters/" + eventId + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                saveEventToFirestore(eventId, title, description, location, date, time, maxParticipants, uri.toString(), tag, callback);
                            }).addOnFailureListener(e -> {
                                callback.onComplete(false, "Failed to get image URL");
                            })
                    )
                    .addOnFailureListener(e -> callback.onComplete(false, "Failed to upload image"));
        } else {
            saveEventToFirestore(eventId, title, description, location, date, time, maxParticipants, null, tag, callback);
        }
    }

    private void saveEventToFirestore(String eventId,
                                      String title,
                                      String description,
                                      String location,
                                      String date,
                                      String time,
                                      int maxParticipants,
                                      String posterUrl,
                                      Event.Tag tag,
                                      UploadCallback callback) {

        long startTimeMillis = 0L;
        try {
            String dateTimeStr = date + " " + time; // e.g., "17/11/2025 14:30"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy HH:mm");
            java.util.Date parsedDate = sdf.parse(dateTimeStr);
            if (parsedDate != null) {
                startTimeMillis = parsedDate.getTime();
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        Event event = new Event(
                eventId,
                title,
                "", // organizerName empty for now
                startTimeMillis,
                location,
                maxParticipants,
                maxParticipants,
                Event.Status.REG_OPEN,
                posterUrl,
                description,
                tag
        );

        eventsRef.document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onComplete(true, "Event posted successfully"))
                .addOnFailureListener(e -> callback.onComplete(false, "Failed to post event"));
    }
    }