package com.example.eventlottery.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
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

                    // Monitor event's status and lists to draw automatically
                    Event.Status oldStatus = event.getStatus();
                    autoDraw(event);
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
        eventsRef.document(eventID)
                .update("waitingList", waitingList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void updateInvitedList(String eventID, List<String> invitedList) {
        eventsRef.document(eventID)
                .update("invitedList", invitedList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void updateAttendeesList(String eventID, List<String> attendeesList) {
        eventsRef.document(eventID)
                .update("attendeesList", attendeesList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void updateCanceledList(String eventID, List<String> canceledList) {
        eventsRef.document(eventID)
                .update("canceledList", canceledList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void joinWaitingListWithLocation(String eventId, String deviceId,
                                            @Nullable Double latitude,
                                            @Nullable Double longitude) {
        Event event = findEventById(eventId);
        if (event == null || deviceId == null) return;

        boolean updated = false;

        if (!event.getWaitingList().contains(deviceId)) {
            event.getWaitingList().add(deviceId);
            updated = true;
        }

        if (event.isGeolocationEnabled() && latitude != null && longitude != null) {
            event.getUserLocations().removeIf(loc -> loc.deviceId.equals(deviceId));
            event.getUserLocations().add(new Event.UserLocation(deviceId, latitude, longitude));
            updated = true;
        }

        if (updated) {
            eventsRef.document(eventId)
                    .update(
                            "waitingList", event.getWaitingList(),
                            "userLocations", event.getUserLocations()
                    )
                    .addOnFailureListener(Throwable::printStackTrace);
        }
    }

    @Override
    public void updateUserLocations(String eventID, List<Event.UserLocation> userLocations) {
        eventsRef.document(eventID)
                .update("userLocations", userLocations)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public static void runAutoDrawLogic(Event event) {
        if (event == null) return;
        if (!event.isRegEnd() || event.isEventStarted()) return;

        Event.Status status = event.getStatus();
        if (status != Event.Status.DRAWN && status != Event.Status.FINALIZED) {
            event.setStatus(Event.Status.DRAWN);
        }

        List<String> invited = new ArrayList<>(event.getInvitedList());
        List<String> attended = new ArrayList<>(event.getAttendeesList());
        List<String> canceled = new ArrayList<>(event.getCanceledList());

        List<String> pending = new ArrayList<>(invited);
        pending.removeAll(attended);
        pending.removeAll(canceled);
        int openSlots = event.getCapacity() - attended.size() - pending.size();
        if (openSlots <= 0) return;

        List<String> pool = new ArrayList<>(event.getWaitingList());
        pool.removeAll(invited);
        pool.removeAll(attended);
        pool.removeAll(canceled);
        if (pool.isEmpty()) return;

        List<String> winners = LotterySystem.drawRounds(pool, openSlots);
        invited.addAll(winners);
        event.setInvitedList(invited);
    }

    public void autoDraw(Event event) {
        if (event == null || event.getId() == null) return;

        List<String> originalInvited = new ArrayList<>(event.getInvitedList());
        runAutoDrawLogic(event);
        List<String> newInvited = event.getInvitedList();
        if (!originalInvited.equals(newInvited)) {
            updateInvitedList(event.getId(), newInvited);
        }
    }

    @Override
    public void deleteEvent(String eventId) {
        eventsRef.document(eventId)
                .delete()
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void removeEventPoster(String eventId) {
        eventsRef.document(eventId)
                .update("posterUrl", null)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // ============================================================
    // UPLOAD EVENT
    // ============================================================
    public interface UploadCallback {
        void onProgress(double progress);
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
            boolean geolocationEnabled,
            @NonNull UploadCallback callback
    ) {
        String eventId = UUID.randomUUID().toString();

        if (imageUri == null) {
            saveEventToFirestore(eventId, title, description, location,
                    eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                    capacity, waitingListSpots, deviceId, null, tag, geolocationEnabled, callback);
            return;
        }

        MediaManager.get().upload(imageUri)
                .unsigned("unsigned_preset")
                .option("public_id", eventId)
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) { callback.onProgress(0.0); }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if (totalBytes > 0) callback.onProgress(bytes / (double) totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("Cloudinary Upload", "Result map: " + resultData);
                        String imageUrl = (String) resultData.get("secure_url");
                        if (imageUrl == null) imageUrl = (String) resultData.get("url");
                        if (imageUrl == null) {
                            callback.onComplete(false, "Upload succeeded but no URL returned", null);
                            return;
                        }
                        saveEventToFirestore(eventId, title, description, location,
                                eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                                capacity, waitingListSpots, deviceId, imageUrl, tag, geolocationEnabled, callback);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onComplete(false, "Failed to upload image: " + error.getDescription(), null);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onComplete(false, "Upload rescheduled: " + error.getDescription(), null);
                    }
                }).dispatch();
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
            boolean geolocationEnabled,
            UploadCallback callback
    ) {
        Event event = new Event(eventId, title, "", eventStartTimeMillis,
                regStartTimeMillis, regEndTimeMillis, location, capacity, waitingListSpots,
                Event.Status.REG_OPEN, posterUrl, description, tag);

        event.setOrganizerId(deviceId);
        event.setGeolocationEnabled(geolocationEnabled);

        eventsRef.document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onComplete(true, "Event posted successfully", eventId))
                .addOnFailureListener(e -> callback.onComplete(false, "Failed to post event: " + e.getMessage(), eventId));
    }

    // ============================================================
    // NEW: GET USER LOCATIONS
    // ============================================================
    @Override
    public void getEventUserLocations(String eventID, @NonNull EventRepository.EventUserLocationsCallback callback) {
        Event event = findEventById(eventID);
        if (event != null && event.getUserLocations() != null) {
            callback.onResult(event.getUserLocations());
        } else {
            callback.onResult(new ArrayList<>());
        }
    }

}