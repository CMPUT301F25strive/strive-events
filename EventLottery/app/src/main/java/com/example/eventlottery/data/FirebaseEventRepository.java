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
                    if (event.getWaitingList() == null)
                        event.setWaitingList(new ArrayList<>());
                    if (event.getAttendeesList() == null)
                        event.setAttendeesList(new ArrayList<>());
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
    public void refresh() {}

    // ============================================================
    // FIND EVENT
    // ============================================================
    @Override
    public Event findEventById(String id) {
        List<Event> list = eventsLiveData.getValue();
        if (list == null) return null;

        for (Event e : list) {
            if (e.getId().equals(id)) return e;
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
    // UPLOAD EVENT — NOW USES DEVICE ID AS ORGANIZER ID
    // ============================================================
    public interface UploadCallback {
        void onComplete(boolean success, String message);
    }

    public void uploadEvent(
            Uri imageUri,
            String title,
            String description,
            String location,
            long startTimeMillis,
            int maxParticipants,
            @NonNull String deviceId,
            @NonNull UploadCallback callback
    ) {

        String eventId = UUID.randomUUID().toString();

        if (imageUri != null) {
            StorageReference imageRef = storage.getReference()
                    .child("event_posters/" + eventId + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                    saveEventToFirestore(eventId, title, description, location,
                                            startTimeMillis, maxParticipants,
                                            deviceId, uri.toString(), callback)
                            ).addOnFailureListener(e ->
                                    callback.onComplete(false, "Failed to get image URL")))
                    .addOnFailureListener(e ->
                            callback.onComplete(false, "Failed to upload image"));
        } else {
            saveEventToFirestore(eventId, title, description, location,
                    startTimeMillis, maxParticipants, deviceId, null, callback);
        }
    }

    private void saveEventToFirestore(
            String eventId,
            String title,
            String description,
            String location,
            long startTimeMillis,
            int maxParticipants,
            String deviceId,
            String posterUrl,
            UploadCallback callback
    ) {

        Event event = new Event(
                eventId,
                title,
                "",             // organizerName deprecated
                startTimeMillis,
                location,
                maxParticipants,
                maxParticipants,
                Event.Status.REG_OPEN,
                posterUrl,
                description
        );

        // DEVICE ID SET HERE
        event.setOrganizerId(deviceId);

        event.setWaitingList(new ArrayList<>());
        event.setAttendeesList(new ArrayList<>());

        eventsRef.document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid ->
                        callback.onComplete(true, "Event posted successfully"))
                .addOnFailureListener(e ->
                        callback.onComplete(false, "Failed to post event"));
    }
}