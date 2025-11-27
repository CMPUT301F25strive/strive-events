package com.example.eventlottery.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Event domain model shared across entrant and organizer screens.
 */
public class Event implements Serializable {

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
    private int waitingListSpots;   // Total spots; -1 == unlimited spots
    private Status status;
    private List<String> waitingList;   // For entrants signing up for the event
    private List<String> attendeesList; // For selected entrants accepting to attend
    private List<String> invitedList;  // For entrants who have ever been selected from Lottery System (02.06.01)
    private List<String> canceledList;  // For selected entrants declining to attend
    private String posterUrl;   // Firebase image URL
    private String description;
    private Tag tag;
    private boolean geolocationEnabled;

    // NEW: Nested list for storing users' locations
    private List<UserLocation> userLocations;

    /**
     * REQUIRED FOR FIRESTORE
     */
    public Event() {
        waitingList = new ArrayList<>();
        attendeesList = new ArrayList<>();
        invitedList = new ArrayList<>();
        canceledList = new ArrayList<>();
        userLocations = new ArrayList<>();
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
        this.canceledList = new ArrayList<>();
        this.invitedList = new ArrayList<>();
        this.userLocations = new ArrayList<>();
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

    public void setTitle(String title) {
        this.title = title;
    }

    @Exclude
    public String getOrganizerName() {
        return organizerName != null ? organizerName : "";
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public long getEventStartTimeMillis() {
        return eventStartTimeMillis;
    }

    public void setEventStartTimeMillis(long eventStartTimeMillis) {
        this.eventStartTimeMillis = eventStartTimeMillis;
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

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWaitingListSpots() {
        return waitingListSpots;
    }

    public void setWaitingListSpots(int waitingListSpots) {
        this.waitingListSpots = waitingListSpots;
    }

    public Status getStatus() {
        return status != null ? status : Status.REG_OPEN;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getWaitingList() {
        if (waitingList == null) waitingList = new ArrayList<>();
        return waitingList;
    }

    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList != null ? waitingList : new ArrayList<>();
    }

    public List<String> getInvitedList() {
        if (invitedList == null) invitedList = new ArrayList<>();
        return invitedList;
    }

    public void setInvitedList(List<String> selectedEntrants) {
        this.invitedList = selectedEntrants != null ? selectedEntrants : new ArrayList<>();
    }

    public List<String> getCanceledList() {
        if (canceledList == null) canceledList = new ArrayList<>();
        return canceledList;
    }

    public void setCanceledList(List<String> canceledList) {
        this.canceledList = canceledList != null ? canceledList : new ArrayList<>();
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

    @Exclude
    public boolean isEventStarted() {
        return System.currentTimeMillis() >= eventStartTimeMillis;
    }

    @Exclude
    public boolean isRegEnd() {
        return System.currentTimeMillis() > regEndTimeMillis;
    }

    @Exclude
    public boolean isRegOpen() {
        return status == Status.REG_OPEN;
    }

    @Exclude
    public boolean isDrawn() { return status == Status.DRAWN; }

    @Exclude
    public boolean isFinalized() { return status == Status.FINALIZED; }

    @Exclude
    public boolean isRegClosed() {
        return status == Status.REG_CLOSED;
    }

    /**
     * WAITING LIST METHODS
     */

    public void joinWaitingList(String deviceId) {
        if (!getWaitingList().contains(deviceId)) {
            waitingList.add(deviceId);
        }
    }

    // UPDATED joinWaitingList WITH GEOLOCATION SUPPORT
    public void joinWaitingList(String deviceId, @Nullable Double latitude, @Nullable Double longitude) {
        if (!getWaitingList().contains(deviceId)) {
            waitingList.add(deviceId);

            if (latitude != null && longitude != null && geolocationEnabled) {
                addUserLocation(deviceId, latitude, longitude);
            }
        }
    }

    public void leaveWaitingList(String deviceId) {
        getWaitingList().remove(deviceId);
    }

    public boolean isOnWaitingList(String deviceId) {
        return getWaitingList().contains(deviceId);
    }

    @Exclude
    public int getWaitingListSize() {
        return getWaitingList().size();
    }

    @Exclude
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

    public boolean isOnAttendeesList(String deviceId) {
        return getAttendeesList().contains(deviceId);
    }

    @Exclude
    public int getAttendeesListSize() {
        return getAttendeesList().size();
    }

    /**
     * GEOLOCATION METHODS
     */

    /**
     * Nested class for storing a device's lat/lon
     */
    public static class UserLocation {
        public String deviceId;
        public Double latitude;
        public Double longitude;

        public UserLocation() {} // Firestore requires no-arg constructor

        public UserLocation(String deviceId, Double latitude, Double longitude) {
            this.deviceId = deviceId;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
    public void setUserLocations(List<UserLocation> userLocations) {
        this.userLocations = userLocations;
    }
    /**
     * Record the user's location only if geolocation is enabled for this event
     */
    public void addUserLocation(String deviceId, double latitude, double longitude) {
        if (!geolocationEnabled) return; // skip if event doesn't need it

        // Remove old entry if exists (update)
        userLocations.removeIf(loc -> loc.deviceId.equals(deviceId));

        // Add new location
        userLocations.add(new UserLocation(deviceId, latitude, longitude));
    }

    /**
     * Get a copy of all user locations
     */
    public List<UserLocation> getUserLocations() {
        if (userLocations == null) userLocations = new ArrayList<>();
        return new ArrayList<>(userLocations); // avoid external modification
    }

    /**
     * EVENT STATUS REFRESH
     */

    public void refreshStatus() {
        long now = System.currentTimeMillis();

        // As soon as it starts, it becomes history, and thus eternal ~
        // Or after all invited entrants accepted (haven't implemented yet)
        if (isEventStarted()) {
            if (status != Status.FINALIZED) {
                status = Status.FINALIZED;
            }
            return; // No need to monitor
        }

        // DRAWN is set after the initial sampling.
        //  It is b/t reg end time and event start time
        //  and before all attendees are confirmed
        if (status == Status.DRAWN && !isEventStarted() && isRegEnd()) {
            return; // Freeze DRAWN status
        }

        // Before registration opens, it's closed
        if (now < regStartTimeMillis && status == Status.REG_OPEN) {
            status = Status.REG_CLOSED;
        }

        // Within registration period
        if (now >= regStartTimeMillis && now <= regEndTimeMillis) {
            // If the waiting list is not full, it's open
            if (!isWaitingListFull() && status == Status.REG_CLOSED) {
                status = Status.REG_OPEN;
            }
            // Otherwise, it's still closed
            else if (isWaitingListFull() && status == Status.REG_OPEN) {
                status = Status.REG_CLOSED;
            }
        }

        // After registration end and before event start, it's closed (before draw)
        if (now >= regEndTimeMillis && status == Status.REG_OPEN) {
            status = Status.REG_CLOSED;
        }
    }

    /**
     * Callback interface for organizer name result
     */
    public interface ProfileNameCallback {
        void onResult(String name);
    }

    public boolean isGeolocationEnabled() { return geolocationEnabled; }
    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }
    /**
     * Remove a user's location entry
     */
    public void removeUserLocation(String deviceId) {
        if (userLocations != null) {
            userLocations.removeIf(loc -> loc.deviceId.equals(deviceId));
        }
    }
}