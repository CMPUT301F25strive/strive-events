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
     * This method updates the list of invited entrants for an event.
     * @param eventID: unique ID of the event
     * @param invitedList: the list of invited entrant IDs
     */
    void updateInvitedList(String eventID, List<String> invitedList);

    void updateAttendeesList(String eventID, List<String> attendeesList);

    void updateCanceledList(String eventID, List<String> canceledList);

    void markInvitationAccepted(String eventId, String deviceId);

    void markInvitationDeclined(String eventId, String deviceId);

    /**
     * Remove an event from the catalogue.
     */
    void deleteEvent(String eventId);

    /**
     * Remove the poster associated with an event.
     */
    void removeEventPoster(String eventId);

    /**
     * This method uses the Lottery System to draw automatically
     * @param event: event object
     */
    void autoDraw(Event event);
    /**
     * Updates the list of user locations for an event.
     * @param eventID event ID
     * @param userLocations list of Event.UserLocation
     */
    void updateUserLocations(String eventID, List<Event.UserLocation> userLocations);

    /**
     * Retrieve user locations for an event.
     * @param eventID: unique ID of the event
     * @param callback: returns list of Event.UserLocation
     */
    void getEventUserLocations(String eventID, @NonNull EventUserLocationsCallback callback);

    interface EventUserLocationsCallback {
        void onResult(List<Event.UserLocation> locations);
    }
}

