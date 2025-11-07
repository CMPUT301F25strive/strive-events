package com.example.eventlottery.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * event domain model shared across entrant and organizer screens.
 */
public class Event implements Serializable {

    /**
     * status values for an event registration window.
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


    /**
     * Event constructor
     * @param id unique device ID
     * @param title event title
     * @param organizerName name of organizer
     * @param startTimeMillis time the event was created
     * @param venue location of the evetn
     * @param spotsRemaining how many spots are left in the waiting list if specified
     * @param status the status of the event ie open, closed, drawn of finalized
     * @param posterResId unique poster ID
     * @param description a description of the event
     */
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
        this.waitingList = new ArrayList<>();
        this.attendeesList = new ArrayList<>();
    }

    /**
     * Gets the unique ID of the event
     * @return unique identifier for this event.
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Gets the title of the event
     * @return display title.
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * Gets the organizer's name
     * @return organizer display name.
     */
    @NonNull
    public String getOrganizerName() {
        return organizerName;
    }

    /**
     * Gets the time created in milliseconds
     * @return event start timestamp in utc millis.
     */
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    /**
     * Gets the name of the venue
     * @return venue description
     */
    @NonNull
    public String getVenue() {
        return venue;
    }

    /**
     * Gets the amount of space the event has
     * @return total capacity configured by the organizer
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the amount of spots left in the waiting list
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

    /**
     * @return description the description of the event
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * @return waitingList the waiting list of the event
     */
    public List<String> getWaitingList() {
        return waitingList;
    }

    /**
     * Sets the waiting list of the event
     * @param waitingList the waiting list of the event
     */
    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    /**
     * adds a user to the waiting list of the event
     * @param deviceId the device ID of the user to be added to the waiting list
     */
    public void joinWaitingList(String deviceId) {
        if (!waitingList.contains(deviceId)) {
            waitingList.add(deviceId);
        }
    }

    /**
     * removes a user from the waiting list of the event
     * @param deviceId the device ID of the user to leave the waiting list
     */
    public void leaveWaitingList(String deviceId) {
        waitingList.remove(deviceId);
    }

    /**
     * @return size: the amount of people in the waiting list of the event
     */
    public int getWaitingListSize() {
        return waitingList.size();
    }

    /**
     * @param deviceId the device ID of the user
     * @return boolean returns true if user is on the waiting list, false otherwise
     */
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
                && Objects.equals(description, event.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, organizerName, startTimeMillis, venue, capacity, spotsRemaining, status, posterResId, description);
    }
}
