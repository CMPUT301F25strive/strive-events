package com.example.eventlottery;

import com.example.eventlottery.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds our Event objects
 */
public class EventRepository {
    private List<Event> events = new ArrayList<>();

    /**
     * This method adds an Event type object to the events list
     * @param e: the object to add
     */
    public void add(Event e) {
        events.add(e);
    }

    /**
     * This method gets the event list
     * @return list: the list of events to be returned
     */
    public List<Event> getEvents() {
        List<Event> list = events;
        return list;
    }

    /**
     * This method gets the event list from an EventID
     * @return Event: the event with the corresponding eventID
     * @throws IllegalArgumentException: if no event is associated with that ID exists
     */
    public Event getEvent(String eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (eventID.equals(events.get(i).getId())) {
                return events.get(i);
            }
        }
        throw new IllegalArgumentException();

    }

    /**
     * This method removes an Event from the list using the event's ID if it exists in the list
     */
    public void deleteEvent(String eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (eventID.equals(events.get(i).getId())) {
                events.remove(events.get(i));
            }
        }
    }

    /**
     * This counts the amount of events in the list
     * @return int: the number of events in the list
     */
    public int countEvents() {
        return events.size();
    }
}
