package com.example.eventlottery.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
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
