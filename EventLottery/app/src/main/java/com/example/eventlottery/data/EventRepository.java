package com.example.eventlottery.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.eventlottery.model.Event;

import java.util.List;

/**
 * entry point for reading events displayed to entrants.
 */
public interface EventRepository {

    /**
     * @return live list of available events.
     */
    @NonNull
    LiveData<List<Event>> observeEvents();

    /**
     * trigger background refresh using the current data source.
     */
    void refresh();

    /**
     * Gets an event from its unique event ID.
     * @param id : unique ID of the event
     * @return the Event whose ID corresponds to the given event ID
     */
    Event findEventById(String id);

    /**
     * updates the waiting list of an event
     * @param eventID : unique ID of the event
     * @param waitingList  the waiting list
     */
    void updateWaitingList(String eventID, List<String> waitingList);

    /**
     * Remove an event from the catalogue.
     */
    void deleteEvent(String eventId);


}
