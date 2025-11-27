package com.example.eventlottery.data;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.LotterySystem;

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
        if (event == null || event.getId() == null) return;

        // Take a snapshot of the current invited list
        List<String> originalInvited = new ArrayList<>(event.getInvitedList());

        // Run the pure logic
        runAutoDrawLogic(event);

        // Only update Firestore if invited list actually changed
        List<String> newInvited = event.getInvitedList();
        if (!originalInvited.equals(newInvited)) {
            updateInvitedList(event.getId(), newInvited);
        }
    }


    /**
     * This method is the pure helper function of checking, executing draws, and updating status to "DRAWN"
     * @param event: the event object
     */
    public static void runAutoDrawLogic(Event event) {
        // Prevent the unknown event
        if (event == null) return;

        // Only run between reg end and event start
        if (!event.isRegEnd() || event.isEventStarted()) {
            return;
        }

        // Set status to DRAWN if no previous draws made and not finalized
        Event.Status status = event.getStatus();
        if (status != Event.Status.DRAWN && status != Event.Status.FINALIZED) {
            event.setStatus(Event.Status.DRAWN);
        }

        // Get the copy of all lists
        List<String> invited = new ArrayList<>(event.getInvitedList());
        List<String> attended = new ArrayList<>(event.getAttendeesList());
        List<String> canceled = new ArrayList<>(event.getCanceledList());

        // Get the pending entrants who haven't made the decision
        List<String> pending = new ArrayList<>(invited);
        pending.removeAll(attended);
        pending.removeAll(canceled);
        int openSlots = event.getCapacity() - attended.size() - pending.size();

        if (openSlots <= 0) {
            // If no more slots to fill, do nothing
            return;
        }

        // Make a sampling pool for entrants who have never been invited
        List<String> pool = new ArrayList<>(event.getWaitingList());
        pool.removeAll(invited);
        pool.removeAll(attended);
        pool.removeAll(canceled);

        if (pool.isEmpty()) {
            // If no one left to invite, do nothing
            return;
        }

        // Run a draw for the specified open slots
        List<String> winners = LotterySystem.drawRounds(pool, openSlots);

        // Add those winners to the invited list
        invited.addAll(winners);
        event.setInvitedList(invited);
    }
}
