package com.example.eventlottery.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventlottery.model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FirebaseEventRepository implements EventRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference eventsRef;
    private final FirebaseStorage storage;

    private final MutableLiveData<List<Event>> eventsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    public FirebaseEventRepository() {
        firestore = FirebaseFirestore.getInstance();
        eventsRef = firestore.collection("events");
        storage = FirebaseStorage.getInstance();

        // Start real-time listener
        listenForEvents();
    }

    // ============================================================
    // REAL-TIME LISTENER
    // ============================================================
    private void listenForEvents() {
        eventsRef.addSnapshotListener((snapshots, e) -> {
            if (snapshots == null) return;

            List<Event> updated = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null) {
                    // Fall back to document ID if missing
                    if (event.getId() == null || event.getId().isEmpty()) {
                        event.setId(doc.getId());
                    }

                    if (event.getWaitingList() == null)
                        event.setWaitingList(new ArrayList<>());
                    if (event.getAttendeesList() == null)
                        event.setAttendeesList(new ArrayList<>());

                    // Monitor the status of event
                    Event.Status oldStatus = event.getStatus();
                    event.refreshStatus();

                    // Sync up the change back to Firestore
                    if (event.getStatus() != oldStatus) {
                        eventsRef.document(event.getId()).update("status", event.getStatus());
                    }
                    updated.add(event);
                }
            }
            eventsLiveData.postValue(updated);
        });
    }

    // ============================================================
    // OBSERVERS
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
    // FIND EVENT
    // ============================================================
    @Override
    public Event findEventById(String id) {
        List<Event> list = eventsLiveData.getValue();
        if (list == null || id == null) return null;

        for (Event e : list) {
            String eventId = e.getId();
            if (id.equals(eventId)) {
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
        void onComplete(boolean success, String message, String eventID);
    }

    public void uploadEvent(
            Uri imageUri,
            String title,
            String description,
            String location,
            long eventStartTimeMillis,
            long regStartTimeMillis,
            long regEndTimeMillis,
            int capacity,
            int waitingListSpots,
            @NonNull String deviceId,
            Event.Tag tag,
            @NonNull UploadCallback callback
    ) {

        // Generate unique event ID
        String eventId = UUID.randomUUID().toString();

        if (imageUri != null) {
            StorageReference imageRef = storage.getReference()
                    .child("event_posters/" + eventId + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                    saveEventToFirestore(eventId, title, description, location,
                                            eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                                            capacity, waitingListSpots, deviceId, uri.toString(), tag, callback)
                            ).addOnFailureListener(e ->
                                    callback.onComplete(false, "Failed to get image URL", null)))
                    .addOnFailureListener(e ->
                            callback.onComplete(false, "Failed to upload image", eventId));
        } else {
            saveEventToFirestore(eventId, title, description, location, eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                    capacity, waitingListSpots, deviceId, null, tag, callback);
        }
    }

    private void saveEventToFirestore(
            String eventId,
            String title,
            String description,
            String location,
            long eventStartTimeMillis,
            long regStartTimeMillis,
            long regEndTimeMillis,
            int capacity,
            int waitingListSpots,
            String deviceId,
            String posterUrl,
            Event.Tag tag,
            UploadCallback callback
    ) {

        Event event = new Event(
                eventId,
                title,
                "", // organizerName empty for now
                eventStartTimeMillis,
                regStartTimeMillis,
                regEndTimeMillis,
                location,
                capacity,
                waitingListSpots,
                Event.Status.REG_OPEN,
                posterUrl,
                description,
                tag
        );

        // DEVICE ID SET HERE
        event.setOrganizerId(deviceId);

        event.setWaitingList(new ArrayList<>());
        event.setAttendeesList(new ArrayList<>());

        eventsRef.document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid ->
                        callback.onComplete(true, "Event posted successfully",eventId))
                .addOnFailureListener(e ->
                        callback.onComplete(false, "Failed to post event", eventId));
    }
}