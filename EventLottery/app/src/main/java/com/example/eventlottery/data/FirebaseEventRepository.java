package com.example.eventlottery.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.LotterySystem;
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
        listenForEvents();
    }

    private void listenForEvents() {
        eventsRef.addSnapshotListener((snapshots, e) -> {
            if (snapshots == null) return;
            List<Event> updated = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null) {
                    if (event.getId() == null) event.setId(doc.getId());
                    if (event.getWaitingList() == null) event.setWaitingList(new ArrayList<>());
                    if (event.getAttendeesList() == null) event.setAttendeesList(new ArrayList<>());
                    Event.Status oldStatus = event.getStatus();
                    event.refreshStatus();
                    if (event.getStatus() != oldStatus) {
                        eventsRef.document(event.getId()).update("status", event.getStatus());
                    }
                    updated.add(event);
                }
            }
            eventsLiveData.postValue(updated);
        });
    }

    @NonNull
    @Override
    public LiveData<List<Event>> observeEvents() { return eventsLiveData; }

    @Override
    public void refresh() { }

    @Override
    public Event findEventById(String id) {
        List<Event> list = eventsLiveData.getValue();
        if (list == null || id == null) return null;
        for (Event e : list) if (id.equals(e.getId())) return e;
        return null;
    }

    @Override
    public void updateWaitingList(String eventID, List<String> waitingList) {
        eventsRef.document(eventID).update("waitingList", waitingList).addOnFailureListener(Throwable::printStackTrace);
    }

    /**
     * This method updates the list of invited entrants for an event.
     *
     * @param eventID     : unique ID of the event
     * @param invitedList : the list of invited entrant IDs
     */
    @Override
    public void updateInvitedList(String eventID, List<String> invitedList) {

    }

    /**
     * This method uses the Lottery System to draw automatically
     * @param eventID : unique ID of the event
     */
    public void autoDraw(String eventID) {
        long now = System.currentTimeMillis();
        Event event = findEventById(eventID);
        if (event == null) return;

        // Run the initial draw only if time period is satisfied and no previous draw
        if (now >= event.getRegEndTimeMillis()
                && !event.isEventStarted()
                && event.getStatus() != Event.Status.DRAWN) {

            List<String> winners = LotterySystem.drawRounds(
                    event.getWaitingList(),
                    event.getCapacity()
            );

            // Set acquired data and update the status to DRAWN
            event.setInvitedList(winners);
            event.setStatus(Event.Status.DRAWN);
            updateInvitedList(eventID, winners);    // Firebase update
        }
        // If time period satisfied and already in DRAWN status, it's a replacement draw
        else if (event.isDrawn()) {
            // Draw a single winner for replacement each time
            String winner = LotterySystem.drawReplacement(
                    event.getWaitingList(),
                    event.getInvitedList(),
                    event.getAttendeesList(),
                    event.getCanceledList()
            );

            List<String> updatedWinners = event.getInvitedList();
            updatedWinners.add(winner);
            event.setInvitedList(updatedWinners);
        }

    }


    @Override
    public void deleteEvent(String eventId) {
        eventsRef.document(eventId).delete().addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void removeEventPoster(String eventId) {
        eventsRef.document(eventId)
                .update("posterUrl", null)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // -------------------
    // Upload Event
    // -------------------
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
            @NonNull UploadCallback callback
    ) {
        String eventId = UUID.randomUUID().toString();

        // If no image, just save event directly
        if (imageUri == null) {
            saveEventToFirestore(eventId, title, description, location,
                    eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                    capacity, waitingListSpots, deviceId, null, tag, callback);
            return;
        }

        // Upload image to Cloudinary with unsigned preset
        MediaManager.get().upload(imageUri)
                .unsigned("unsigned_preset")
                .option("folder", "eventlottery/event_posters")
                .option("public_id", eventId)
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        callback.onProgress(0.0);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if (totalBytes > 0) callback.onProgress(bytes / (double) totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Debug log to check what Cloudinary returns
                        Log.d("Cloudinary Upload", "Result map: " + resultData);

                        // Try secure_url, fallback to url
                        String imageUrl = (String) resultData.get("secure_url");
                        if (imageUrl == null) imageUrl = (String) resultData.get("url");

                        if (imageUrl == null) {
                            callback.onComplete(false, "Upload succeeded but no URL returned", null);
                            return;
                        }

                        // Save event with posterUrl
                        saveEventToFirestore(eventId, title, description, location,
                                eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                                capacity, waitingListSpots, deviceId, imageUrl, tag, callback);
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
            UploadCallback callback
    ) {
        Event event = new Event(eventId, title, "", eventStartTimeMillis,
                regStartTimeMillis, regEndTimeMillis, location, capacity, waitingListSpots,
                Event.Status.REG_OPEN, posterUrl, description, tag);
        event.setOrganizerId(deviceId);

        eventsRef.document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onComplete(true, "Event posted successfully", eventId))
                .addOnFailureListener(e -> callback.onComplete(false, "Failed to post event: " + e.getMessage(), eventId));
    }
}