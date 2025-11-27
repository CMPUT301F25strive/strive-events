package com.example.eventlottery.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>(new ArrayList<>());

    @Override
    public void refresh() {
        // No-op for mock, post current state
        eventsLiveData.postValue(new ArrayList<>(events));
    }

    @Override
    public void updateUserLocations(String eventID, List<Event.UserLocation> userLocations) {
        Event event = findEventById(eventID);
        if (event != null) {
            event.setUserLocations(userLocations);
            refresh();
        }
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
        refresh();
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
        refresh();
    }

    @Override
    public void removeEventPoster(String eventId) {
        Event event = findEventById(eventId);
        if (event != null) {
            event.setPosterUrl(null);
            refresh();
        }
    }

    public int getSize() {
        return events.size();
    }

    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }

    @Override
    public LiveData<List<Event>> observeEvents() {
        return eventsLiveData;
    }

    @Override
    public void updateWaitingList(String eventID, List<String> waitingList) {
        Event event = findEventById(eventID);
        if (event != null) {
            event.setWaitingList(waitingList);
            refresh();
        }
    }

    @Override
    public void updateInvitedList(String eventID, List<String> invitedList) {
        Event event = findEventById(eventID);
        if (event != null) {
            event.setInvitedList(invitedList);
            refresh();
        }
    }

    @Override
    public void updateAttendeesList(String eventID, List<String> attendeesList) {
        Event event = findEventById(eventID);
        if (event != null) {
            event.setAttendeesList(attendeesList);
            refresh();
        }
    }

    @Override
    public void updateCanceledList(String eventID, List<String> canceledList) {
        Event event = findEventById(eventID);
        if (event != null) {
            event.setCanceledList(canceledList);
            refresh();
        }
    }

    /**
     * This method uses the Lottery System to draw automatically
     *
     * @param event : event object
     */
    @Override
    public void autoDraw(Event event) {
        if (event == null || event.getId() == null) return;

        List<String> originalInvited = new ArrayList<>(event.getInvitedList());
        runAutoDrawLogic(event);
        List<String> newInvited = event.getInvitedList();
        if (!originalInvited.equals(newInvited)) {
            updateInvitedList(event.getId(), newInvited);
        }
    }

    /**
     * Pure helper for checking, executing draws, updating status
     *
     * @param event: the event object
     */
    public static void runAutoDrawLogic(Event event) {
        if (event == null) return;
        if (!event.isRegEnd() || event.isEventStarted()) return;

        Event.Status status = event.getStatus();
        if (status != Event.Status.DRAWN && status != Event.Status.FINALIZED) {
            event.setStatus(Event.Status.DRAWN);
        }

        List<String> invited = new ArrayList<>(event.getInvitedList());
        List<String> attended = new ArrayList<>(event.getAttendeesList());
        List<String> canceled = new ArrayList<>(event.getCanceledList());

        List<String> pending = new ArrayList<>(invited);
        pending.removeAll(attended);
        pending.removeAll(canceled);
        int openSlots = event.getCapacity() - attended.size() - pending.size();
        if (openSlots <= 0) return;

        List<String> pool = new ArrayList<>(event.getWaitingList());
        pool.removeAll(invited);
        pool.removeAll(attended);
        pool.removeAll(canceled);
        if (pool.isEmpty()) return;

        List<String> winners = LotterySystem.drawRounds(pool, openSlots);
        invited.addAll(winners);
        event.setInvitedList(invited);
    }

    /**
     * Adds a device to the waiting list and optionally records geolocation
     */
    public void joinWaitingListWithLocation(String eventId, String deviceId,
                                            Double latitude,
                                            Double longitude) {
        Event event = findEventById(eventId);
        if (event == null || deviceId == null) return;

        boolean updated = false;

        if (!event.getWaitingList().contains(deviceId)) {
            event.getWaitingList().add(deviceId);
            updated = true;
        }

        if (event.isGeolocationEnabled() && latitude != null && longitude != null) {
            if (event.getUserLocations() == null) event.setUserLocations(new ArrayList<>());
            event.getUserLocations().removeIf(loc -> loc.deviceId.equals(deviceId));
            event.getUserLocations().add(new Event.UserLocation(deviceId, latitude, longitude));
            updated = true;
        }

        if (updated) refresh();
    }

    /**
     * NEW: get user locations for an event
     */
    public void getEventUserLocations(String eventID, @NonNull EventRepository.EventUserLocationsCallback callback) {
        Event event = findEventById(eventID);
        if (event != null && event.getUserLocations() != null) {
            callback.onResult(event.getUserLocations());
        } else {
            callback.onResult(new ArrayList<>());
        }
    }
}