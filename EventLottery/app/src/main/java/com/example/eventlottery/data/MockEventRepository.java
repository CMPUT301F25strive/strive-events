package com.example.eventlottery.data;

import com.example.eventlottery.model.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A mock version of FirebaseEventRepository for local unit testing.
 * It keeps data in memory and does not use Firebase.
 */
public class MockEventRepository implements EventRepository {
    private final List<Event> events = new ArrayList<>();

    @Override
    public void refresh() {
    }

    @Override
    public Event findEventById(String eventID) {
        for (Event e : events) {
            if (Objects.equals(e.getId(), eventID)) {
                return e;
            }
        }
        return null;
    }

    public void add(Event e) {
        events.add(e);
    }

    public Event getEvent(String eventID) {
        for (Event e : events) {
            if (Objects.equals(e.getId(), eventID)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Event not found: " + eventID);
    }

    @Override
    public void deleteEvent(String eventID) {
        events.removeIf(e -> Objects.equals(e.getId(), eventID));
    }

    @Override
    public void removeEventPoster(String eventId) {
        Event event = findEventById(eventId);
        if (event != null) {
            event.setPosterUrl(null);
        }
    }

    public int getSize() {
        return events.size();
    }

    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }

    @Override
    public androidx.lifecycle.LiveData<List<Event>> observeEvents() {
        return null;
    }

    @Override
    public void updateWaitingList(String eventID, List<String> waitingList) {
    }

    /**
     * This method updates the list of invited entrants for an event.
     *
     * @param eventID     : unique ID of the event
     * @param invitedList : the list of invited entrant IDs
     */
    @Override
    public void updateInvitedList(String eventID, List<String> invitedList) {

    }

    @Override
    public void updateAttendeesList(String eventID, List<String> attendeesList) {

    }

    @Override
    public void updateCanceledList(String eventID, List<String> canceledList) {

    }

    /**
     * This method uses the Lottery System to draw automatically
     *
     * @param event : event object
     */
    @Override
    public void autoDraw(Event event) {

    }
}
