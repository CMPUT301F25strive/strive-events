package com.example.eventlottery.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * event domain model shared across entrant and organizer screens.
 */
public class Event {

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
    @DrawableRes
    private final int posterResId;

    public Event(
            @NonNull String id,
            @NonNull String title,
            @NonNull String organizerName,
            long startTimeMillis,
            @NonNull String venue,
            int capacity,
            int spotsRemaining,
            @NonNull Status status,
            @DrawableRes int posterResId
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return startTimeMillis == event.startTimeMillis
                && capacity == event.capacity
                && spotsRemaining == event.spotsRemaining
                && posterResId == event.posterResId
                && id.equals(event.id)
                && title.equals(event.title)
                && organizerName.equals(event.organizerName)
                && venue.equals(event.venue)
                && status == event.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, organizerName, startTimeMillis, venue, capacity, spotsRemaining, status, posterResId);
    }
}
