package com.example.eventlottery.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private String id;
    private String title;
    private String organizerName;
    private long startTimeMillis;
    private String venue;
    private int capacity;
    private int spotsRemaining;
    private Status status;
    private List<String> waitingList;
    private List<String> attendeesList;
    private String posterUrl;   // ✅ NEW — stores Firebase image URL
    private String description;

    /** REQUIRED FOR FIRESTORE */
    public Event() {
        waitingList = new ArrayList<>();
        attendeesList = new ArrayList<>();
    }

    /** Normal constructor */
    public Event(
            @NonNull String id,
            @NonNull String title,
            @NonNull String organizerName,
            long startTimeMillis,
            @NonNull String venue,
            int capacity,
            int spotsRemaining,
            @NonNull Status status,
            @Nullable String posterUrl,
            @Nullable String description
    ) {
        this.id = id;
        this.title = title;
        this.organizerName = organizerName;
        this.startTimeMillis = startTimeMillis;
        this.venue = venue;
        this.capacity = capacity;
        this.spotsRemaining = spotsRemaining;
        this.status = status;
        this.posterUrl = posterUrl;
        this.description = description;
        this.waitingList = new ArrayList<>();
        this.attendeesList = new ArrayList<>();
    }

    /** GETTERS */

    public String getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getOrganizerName() { return organizerName != null ? organizerName : ""; }
    public long getStartTimeMillis() { return startTimeMillis; }
    public String getVenue() { return venue != null ? venue : ""; }
    public int getCapacity() { return capacity; }
    public int getSpotsRemaining() { return spotsRemaining; }
    public Status getStatus() { return status != null ? status : Status.REG_OPEN; }

    public String getPosterUrl() { return posterUrl; }   // ✅ NEW getter
    public String getDescription() { return description; }

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

    /** WAITING LIST METHODS */

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

    /** ATTENDEE LIST METHODS */

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
}