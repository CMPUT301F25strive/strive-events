package com.example.eventlottery.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Event domain model shared across entrant and organizer screens.
 */
public class Event implements Serializable {

    // TODO: state machine?
    public enum Status {
        REG_OPEN,
        REG_CLOSED,
        DRAWN,
        FINALIZED
    }

    public enum Tag {
        ART,
        MUSIC,
        EDUCATION,
        SPORTS,
        PARTY
    }


    private String id;
    private String title;

    private String organizerId;

    private String organizerName;
    private long eventStartTimeMillis;
    private long regStartTimeMillis;
    private long regEndTimeMillis;
    private String venue;
    private int capacity;   // The number to sample
    private int waitingListSpots;   // 0 == unlimited spots
    private Status status;
    private List<String> waitingList;
    private List<String> attendeesList;
    private String posterUrl;   // Firebase image URL
    private String description;
    private Tag tag;

    /**
     * REQUIRED FOR FIRESTORE
     */
    public Event() {
        waitingList = new ArrayList<>();
        attendeesList = new ArrayList<>();
    }

    /**
     * Normal constructor
     */
    public Event(
            @NonNull String id,
            @NonNull String title,
            @NonNull String organizerName,
            long eventStartTimeMillis,
            long regStartTimeMillis,
            long regEndTimeMillis,
            @NonNull String venue,
            int capacity,
            int waitingListSpots,
            @NonNull Status status,
            @Nullable String posterUrl,
            @Nullable String description,
            @NonNull Tag tag
    ) {
        this.id = id;
        this.title = title;
        this.organizerName = organizerName;
        this.eventStartTimeMillis = eventStartTimeMillis;
        this.regStartTimeMillis = regStartTimeMillis;
        this.regEndTimeMillis = regEndTimeMillis;
        this.venue = venue;
        this.capacity = capacity;
        this.waitingListSpots = waitingListSpots;
        this.status = status;
        this.tag = tag;
        this.posterUrl = posterUrl;
        this.description = description;
        this.waitingList = new ArrayList<>();
        this.attendeesList = new ArrayList<>();
    }

    /**
     * GETTERS
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getOrganizerName() {
        return organizerName != null ? organizerName : "";
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public long getEventStartTimeMillis() {
        return eventStartTimeMillis;
    }

    public long getRegEndTimeMillis() {
        return regEndTimeMillis;
    }

    public void setRegEndTimeMillis(long regEndTimeMillis) {
        this.regEndTimeMillis = regEndTimeMillis;
    }

    public long getRegStartTimeMillis() {
        return regStartTimeMillis;
    }

    public void setRegStartTimeMillis(long regStartTimeMillis) {
        this.regStartTimeMillis = regStartTimeMillis;
    }

    public String getVenue() {
        return venue != null ? venue : "";
    }

    public int getCapacity() {
        return capacity;
    }

    public int getwaitingListSpots() {
        return waitingListSpots;
    }

    public Status getStatus() {
        return status != null ? status : Status.REG_OPEN;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getWaitingList() {
        if (waitingList == null) waitingList = new ArrayList<>();
        return waitingList;
    }

    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList != null ? waitingList : new ArrayList<>();
    }

    public List<String> getAttendeesList() {
        if (attendeesList == null) attendeesList = new ArrayList<>();
        return attendeesList;
    }

    public void setAttendeesList(List<String> attendeesList) {
        this.attendeesList = attendeesList != null ? attendeesList : new ArrayList<>();
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public boolean isEventStarted() {
        return System.currentTimeMillis() >= eventStartTimeMillis;
    }

    public boolean isRegStarted() {
        return System.currentTimeMillis() >= regStartTimeMillis;
    }

    public boolean isRegClosed() {
        return System.currentTimeMillis() >= regEndTimeMillis;
    }

    public boolean isRegPeriod() {
        return getStatus() == Status.REG_OPEN
                && isRegStarted()
                && !isRegClosed()
                && !isEventStarted();
    }

    /**
     * WAITING LIST METHODS
     */

    public void joinWaitingList(String deviceId) {
        if (!getWaitingList().contains(deviceId)) {
            waitingList.add(deviceId);
        }
    }

    public void leaveWaitingList(String deviceId) {
        getWaitingList().remove(deviceId);
    }

    public boolean isOnWaitingList(String deviceId) {
        return getWaitingList().contains(deviceId);
    }

    public int getWaitingListSize() {
        return getWaitingList().size();
    }

    public boolean isWaitingListFull() {
        if (waitingListSpots < 0) return false; // Let -1 represent unlimited
        return getWaitingListSize() >= waitingListSpots;
    }

    /**
     * ATTENDEE LIST METHODS
     */

    public void joinAttendeesList(String deviceId) {
        if (!getAttendeesList().contains(deviceId)) {
            attendeesList.add(deviceId);
        }
    }

    public void leaveAttendeesList(String deviceId) {
        getAttendeesList().remove(deviceId);
    }

    public int getAttendeesListSize() {
        return getAttendeesList().size();
    }

    /**
     * Asynchronously fetches the organizer's current profile name
     * using the organizerId stored in this event.
     *
     * @param callback returns the organizer name, or "Unknown Organizer" on error.
     */
    public void fetchOrganizerName(ProfileNameCallback callback) {
        if (organizerId == null || organizerId.trim().isEmpty()) {
            callback.onResult("Unknown Organizer");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("profiles")
                .document(organizerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        callback.onResult(name != null ? name : "Unknown Organizer");
                    } else {
                        callback.onResult("Unknown Organizer");
                    }
                })
                .addOnFailureListener(e -> callback.onResult("Unknown Organizer"));
    }

    public void refreshStatus() {
        long now = System.currentTimeMillis();

        // As soon as it starts, it becomes history, and thus eternal ~
            // Or after all candidates accepted (associated with lottery sys)
        if (now >= eventStartTimeMillis) {
            if (status != Status.FINALIZED) {
                status = Status.FINALIZED;
            }
            return; // No need to monitor
        }

        // Before registration opens, it's closed
        if (now < regStartTimeMillis && status == Status.REG_OPEN) {
            status = Status.REG_CLOSED;
        }

        // Within registration period, it's open
        if (now >= regStartTimeMillis && now <= regEndTimeMillis
                && status == Status.REG_CLOSED) {
            status = Status.REG_OPEN;
        }

        // After registration end and before event start, it's closed (before draw)
        if (now >= regEndTimeMillis && status == Status.REG_OPEN) {
            status = Status.REG_CLOSED;
        }

        // TODO: figure out what things to do during DRAWN,
        //  like b/t reg end time and event start time
        //  or before all attendees are confirmed
    }


    /**
     * Callback interface for organizer name result
     */
    public interface ProfileNameCallback {
        void onResult(String name);
    }
}