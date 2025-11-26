package com.example.eventlottery.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.example.eventlottery.model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FirebaseEventRepository implements EventRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference eventsRef;
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>(new ArrayList<>());

    public FirebaseEventRepository() {
        firestore = FirebaseFirestore.getInstance();
        eventsRef = firestore.collection("events");

        // Start real-time listener for events
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
                    if (event.getId() == null) event.setId(doc.getId());

                    // Initialize empty lists if null
                    if (event.getWaitingList() == null) event.setWaitingList(new ArrayList<>());
                    if (event.getAttendeesList() == null) event.setAttendeesList(new ArrayList<>());

                    // Monitor event status
                    Event.Status oldStatus = event.getStatus();
                    event.refreshStatus();

                    // Update Firestore if status changed
                    if (event.getStatus() != oldStatus) {
                        eventsRef.document(event.getId()).update("status", event.getStatus());
                    }

                    updated.add(event);
                }
            }

            // Post updated list to LiveData
            eventsLiveData.postValue(updated);
        });
    }

    // ============================================================
    // OBSERVERS
    // ============================================================
    @NonNull
    @Override
    public LiveData<List<Event>> observeEvents() { return eventsLiveData; }

    @Override
    public void refresh() {
        // Firestore is real-time; manual refresh not required
    }

    // ============================================================
    // FIND EVENT
    // ============================================================
    @Override
    public Event findEventById(String id) {
        List<Event> list = eventsLiveData.getValue();
        if (list == null || id == null) return null;

        // Search for event in current list
        for (Event e : list) if (id.equals(e.getId())) return e;
        return null;
    }

    // ============================================================
    // UPDATE WAITING LIST
    // ============================================================
    @Override
    public void updateWaitingList(String eventID, List<String> waitingList) {
        // Update waiting list in Firestore
        eventsRef.document(eventID)
                .update("waitingList", waitingList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // ============================================================
    // DELETE EVENT
    // ============================================================
    @Override
    public void deleteEvent(String eventId) {
        // Remove event document from Firestore
        eventsRef.document(eventId)
                .delete()
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void removeEventPoster(String eventId) {
        // Clear poster URL in Firestore
        eventsRef.document(eventId)
                .update("posterUrl", null)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // ============================================================
    // UPLOAD EVENT
    // ============================================================
    public interface UploadCallback {
        void onProgress(double progress); // 0.0 to 1.0
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
            boolean geolocationEnabled,   // ⚡️ KEEP THIS
            @NonNull UploadCallback callback
    ) {
        // Generate unique event ID
        String eventId = UUID.randomUUID().toString();

        // If no image, save event directly
        if (imageUri == null) {
            saveEventToFirestore(eventId, title, description, location,
                    eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                    capacity, waitingListSpots, deviceId, null, tag, geolocationEnabled, callback); // ⚡️ ADD HERE
            return;
        }

        // Upload image to Cloudinary with unsigned preset
        MediaManager.get().upload(imageUri)
                .unsigned("unsigned_preset")
                .option("public_id", eventId)
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Notify 0% progress at start
                        callback.onProgress(0.0);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if (totalBytes > 0) callback.onProgress(bytes / (double) totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Debug log for Cloudinary response
                        Log.d("Cloudinary Upload", "Result map: " + resultData);

                        // Try secure_url first, fallback to url
                        String imageUrl = (String) resultData.get("secure_url");
                        if (imageUrl == null) imageUrl = (String) resultData.get("url");

                        if (imageUrl == null) {
                            // Fail if no URL returned
                            callback.onComplete(false, "Upload succeeded but no URL returned", null);
                            return;
                        }

                        // Save event with poster URL
                        saveEventToFirestore(eventId, title, description, location,
                                eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                                capacity, waitingListSpots, deviceId, imageUrl, tag, geolocationEnabled, callback); // ⚡️ ADD HERE
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onComplete(false, "Failed to upload image: " + error.getDescription(), null);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onComplete(false, "Upload rescheduled: " + error.getDescription(), null);
                    }
                })
                .dispatch();
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
            boolean geolocationEnabled,  // ⚡️ RECEIVED HERE
            UploadCallback callback
    ) {
        // Create event object
        Event event = new Event(eventId, title, "", eventStartTimeMillis,
                regStartTimeMillis, regEndTimeMillis, location, capacity, waitingListSpots,
                Event.Status.REG_OPEN, posterUrl, description, tag);

        // Set device ID of organizer
        event.setOrganizerId(deviceId);

        // ⚡️ SAVE GEOLOCATION FLAG
        event.setGeolocationEnabled(geolocationEnabled);

        // Save event to Firestore
        eventsRef.document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onComplete(true, "Event posted successfully", eventId))
                .addOnFailureListener(e -> callback.onComplete(false, "Failed to post event: " + e.getMessage(), eventId));
    }
}