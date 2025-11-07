package com.example.eventlottery.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.eventlottery.model.Event;

import java.util.List;

/**
 * Entry point for reading events displayed to entrants.
 * Includes support for observing and updating the waiting list.
 */
public interface EventRepository {

    /**
     * @return live list of available events.
     */
    @NonNull
    LiveData<List<Event>> observeEvents();

    /**
     * Trigger background refresh using the current data source.
     */
    void refresh();

    /**
     * Find a single event by its unique ID.
     * @param id event identifier
     * @return event or null if not found
     */
    Event findEventById(String id);

    /**
     * Update the waiting list for a specific event.
     * @param eventID the event's ID
     * @param waitingList list of device IDs on the waiting list
     */
    void updateWaitingList(@NonNull String eventID, @NonNull List<String> waitingList);

    /**
     * Remove an event from the catalogue.
     */
    void deleteEvent(@NonNull String eventId);

}