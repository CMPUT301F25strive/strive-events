package com.example.eventlottery.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Event domain model shared across entrant and organizer screens.
 */
public class Event implements Serializable {

    /**
     * Status values for an event registration window.
     */
    public enum Status {
        REG_OPEN,
        REG_CLOSED,
        DRAWN,
        FINALIZED
    }

    private final String id;
    private final String title;
    private final String organizerName;
    private final long startTimeMillis;
    private final String venue;
    private final int capacity;
    private final int spotsRemaining;
    private final Status status;
    private List<String> waitingList;
    private List<String> attendeesList;
    @DrawableRes
    private final int posterResId;
    @Nullable
    private final String description;

    public Event(
            @NonNull String id,
            @NonNull String title,
            @NonNull String organizerName,
            long startTimeMillis,
            @NonNull String venue,
            int capacity,
            int spotsRemaining,
            @NonNull Status status,
            @DrawableRes int posterResId,
            @Nullable String description
    ) {
        this.id = Objects.requireNonNull(id, "id required");
        this.title = Objects.requireNonNull(title, "title required");
        this.organizerName = Objects.requireNonNull(organizerName, "organizerName required");
        this.startTimeMillis = startTimeMillis;
        this.venue = Objects.requireNonNull(venue, "venue required");
        this.capacity = capacity;
        this.spotsRemaining = spotsRemaining;
        this.status = Objects.requireNonNull(status, "status required");
        this.posterResId = posterResId;
        this.description = description;
        this.attendeesList = new ArrayList<>();
        this.waitingList = waitingList != null ? waitingList : new ArrayList<>();
    }

    /**
     * @return unique identifier for this event.
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * @return display title.
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * @return organizer display name.
     */
    @NonNull
    public String getOrganizerName() {
        return organizerName;
    }

    /**
     * @return event start timestamp in utc millis.
     */
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    /**
     * @return venue description
     */
    @NonNull
    public String getVenue() {
        return venue;
    }

    /**
     * @return total capacity configured by the organizer
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @return current remaining entries on the waiting list.
     */
    public int getSpotsRemaining() {
        return spotsRemaining;
    }

    /**
     * @return current registration status.
     */
    @NonNull
    public Status getStatus() {
        return status;
    }

    /**
     * @return drawable resource used as poster placeholder.
     */
    @DrawableRes
    public int getPosterResId() {
        return posterResId;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public List<String> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    public void joinWaitingList(String deviceId) {
        if (!waitingList.contains(deviceId)) {
            waitingList.add(deviceId);
        }
    }

    public void leaveWaitingList(String deviceId) {
        waitingList.remove(deviceId);
    }

    public int getWaitingListSize() {
        return waitingList.size();
    }

    public boolean isOnWaitingList(String deviceId) {
        return attendeesList.contains(deviceId);
    }

    public void joinAttendeesList(String deviceId) {
        if (!attendeesList.contains(deviceId)) {
            attendeesList.add(deviceId);
        }
    }

    public void leaveAttendeesList(String deviceId) {
        attendeesList.remove(deviceId);
    }

    public int getAttendeesListSize() {
        return attendeesList.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return startTimeMillis == event.startTimeMillis
                && capacity == event.capacity
                && spotsRemaining == event.spotsRemaining
                && posterResId == event.posterResId
                && Objects.equals(id, event.id)
                && Objects.equals(title, event.title)
                && Objects.equals(organizerName, event.organizerName)
                && Objects.equals(venue, event.venue)
                && status == event.status
                && Objects.equals(description, event.description)
                && Objects.equals(waitingList, event.waitingList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, organizerName, startTimeMillis, venue, capacity, spotsRemaining, status, posterResId, description, waitingList);
    }
}