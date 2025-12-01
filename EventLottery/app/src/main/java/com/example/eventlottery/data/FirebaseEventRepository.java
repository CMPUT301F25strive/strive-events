package com.example.eventlottery.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.example.eventlottery.model.AppContextProvider;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.InvitationService;
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
                    event.refreshStatus();
                    // Checks if its the first time being drawn to draw entrants
                    boolean firstTimeDraw = oldStatus != Event.Status.DRAWN && event.getStatus() == Event.Status.DRAWN;
                    // Checks if the invited list has less than the capacity and can be filled up from the waiting list
                    boolean refillSlots = event.getStatus() == Event.Status.DRAWN && event.getInvitedList().size() < event.getCapacity() && !event.getWaitingList().isEmpty();
                    if (firstTimeDraw || refillSlots) {
                        autoDraw(event);
                    }

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

    /**
     * This method updates the list of invited entrants for an event.
     *
     * @param eventID     : unique ID of the event
     * @param invitedList : the list of invited entrant IDs
     */
    @Override
    public void updateInvitedList(String eventID, List<String> invitedList) {
        // Update invited entrants list in Firestore
        eventsRef.document(eventID)
                .update("invitedList", invitedList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void updateAttendeesList(String eventID, List<String> attendeesList) {
        // Update attendees list in Firestore
        eventsRef.document(eventID)
                .update("attendeesList", attendeesList)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void updateCanceledList(String eventID, List<String> canceledList) {
        // Update canceled entrants list in Firestore
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

    /**
     * This method is the pure helper function of checking, executing draws, and updating status to "DRAWN"
     * @param event: the event object
     */
    public static void runAutoDrawLogic(Event event) {
        runAutoDrawLogic(event, false);
    }

    public static void runAutoDrawLogic(Event event, boolean ignoreRegEndConstraint) {
        if (event == null) return;

        if (event.isEventStarted()) return;
        if (!ignoreRegEndConstraint && !event.isRegEnd()) {
            return;
        }

        // Set status to DRAWN if no previous draws made and not finalized
        Event.Status status = event.getStatus();
        if (status != Event.Status.DRAWN && status != Event.Status.FINALIZED) {
            event.setStatus(Event.Status.DRAWN);
        }

        // Get the copy of all lists
        List<String> invited = new ArrayList<>(event.getInvitedList());
        List<String> attended = new ArrayList<>(event.getAttendeesList());
        List<String> canceled = new ArrayList<>(event.getCanceledList());

        // Get the pending entrants who haven't made the decision
        List<String> pending = new ArrayList<>(invited);
        pending.removeAll(attended);
        pending.removeAll(canceled);
        int openSlots = event.getCapacity() - attended.size() - pending.size();

        if (openSlots <= 0) {
            // If no more slots to fill, do nothing
            return;
        }

        // Make a sampling pool for entrants who have never been invited
        List<String> pool = new ArrayList<>(event.getWaitingList());
        pool.removeAll(invited);
        pool.removeAll(attended);
        pool.removeAll(canceled);

        if (pool.isEmpty()) {
            // If no one left to invite, do nothing
            return;
        }

        // Run a draw for the specified open slots
        List<String> winners = LotterySystem.drawRounds(pool, openSlots);

        // Add those winners to the invited list
        invited.addAll(winners);
        event.setInvitedList(invited);
    }


    /**
     * This method uses the Lottery System to draw automatically
     * @param event : the event object
     */
    public void autoDraw(Event event) {
        executeDraw(event, false, false);
    }

    @Override
    public void manualDraw(Event event) {
        executeDraw(event, true, true);
    }

    private void executeDraw(@Nullable Event event,
                             boolean ignoreRegEndConstraint,
                             boolean updateStatusInFirestore) {
        if (event == null || event.getId() == null) return;

        List<String> originalInvited = new ArrayList<>(event.getInvitedList());
        Event.Status originalStatus = event.getStatus();

        runAutoDrawLogic(event, ignoreRegEndConstraint);

        List<String> newInvited = event.getInvitedList();
        if (!originalInvited.equals(newInvited)) {

            // Detect newly invited users
            List<String> newlyInvited = new ArrayList<>(newInvited);
            newlyInvited.removeAll(originalInvited);

            // Detect losers who did not make the invited list
            List<String> loserNonInvited = new ArrayList<>(event.getWaitingList());
            List<String> cancelled = event.getCanceledList();
            List<String> accepted = event.getAttendeesList();
            loserNonInvited.removeAll(newInvited);
            loserNonInvited.removeAll(cancelled);
            loserNonInvited.removeAll(accepted);

            // Send notifications only to the newly invited users
            InvitationService invitationService = new InvitationService(AppContextProvider.getContext());
            invitationService.sendWinnerInvitations(
                    newlyInvited,
                    event.getOrganizerId(),
                    event.getTitle()
            );
            // Send notifications only to the rest of the waiting list who did not get chosen
            invitationService.sendLoserInvitations(
                    loserNonInvited,
                    event.getOrganizerId(),
                    event.getTitle()
            );

            // Update Firestore invited list in event
            updateInvitedList(event.getId(), newInvited);
        }

        if (updateStatusInFirestore && originalStatus != event.getStatus()) {
            eventsRef.document(event.getId())
                    .update("status", event.getStatus())
                    .addOnFailureListener(Throwable::printStackTrace);
        }
    }

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
            boolean geolocationEnabled,
            @NonNull UploadCallback callback
    ) {
        // Generate unique event ID
        String eventId = UUID.randomUUID().toString();

        // If no image, save event directly
        if (imageUri == null) {
            saveEventToFirestore(eventId, title, description, location,
                    eventStartTimeMillis, regStartTimeMillis, regEndTimeMillis,
                    capacity, waitingListSpots, deviceId, null, tag, geolocationEnabled, callback);
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
        // Create event object
        Event event = new Event(eventId, title, "", eventStartTimeMillis,
                regStartTimeMillis, regEndTimeMillis, location, capacity, waitingListSpots,
                Event.Status.REG_OPEN, posterUrl, description, tag);

        // Set device ID of organizer
        event.setOrganizerId(deviceId);

        // SAVE GEOLOCATION FLAG
        event.setGeolocationEnabled(geolocationEnabled);

        // Save event to Firestore
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
