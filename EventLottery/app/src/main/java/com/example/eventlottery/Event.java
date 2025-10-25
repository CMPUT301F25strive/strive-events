package com.example.eventlottery;

/**
 * This is a class representing an Event object
 */
public class Event {
    private int eventID;
    private String name;
    private String description;
    private String venue;
    private int capacity;
    private float price;
    private int organizerID;
    private int posterID;
    private boolean geoRequired;


    /**
     * This method constructs an event object
     * @param eventID: the ID of the event, event list size + 1
     * @param name: the name of the event
     * @param description: the description of the event
     * @param venue: the venue for the event
     * @param capacity: the total number of people that can be selected for the event
     * @param price: the cost for the entrant if selected for the event
     * @param organizerID: the ID of the organizer
     * @param posterID: the ID of the poster the event uses
     * @param geoRequired: boolean that says if location matters in selection
     */
    public Event(int eventID, String name, String description, String venue, int capacity, float price, int organizerID, int posterID, boolean geoRequired) {
        this.eventID = eventID;
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.capacity = capacity;
        this.price = price;
        this.organizerID = organizerID;
        this.posterID = posterID;
        this.geoRequired = geoRequired;
    }

    /**
     * This method gets the ID of the event
     * @return eventID: the ID of the event
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * This method gets the name of the event
     * @return name: the name of the event
     */
    public String getName() {
        return name;
    }

    /**
     * This method gets the description of the event
     * @return description: the description of the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method gets the venue of the event
     * @return venue: the venue of the event
     */
    public String getVenue() {
        return venue;
    }

    /**
     * This method gets the capacity of the event
     * @return capacity: the capacity of the event
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * This method gets the price of the event
     * @return price: the price of the event
     */
    public float getPrice() {
        return price;
    }

    /**
     * This method gets the ID of the event's organizer
     * @return organizerID: the ID of the event's organizer
     */
    public int getOrganizerID() {
        return organizerID;
    }

    /**
     * This method gets the ID of the event's poster
     * @return posterID: the ID of the event's poster
     */
    public int getPosterID() {
        return posterID;
    }

    /**
     * This method gets if the geolocation matters for entrants to this event
     * @return geoRequired: the boolean saying if geolocation matters
     */
    public boolean getGeoRequired() {
        return geoRequired;
    }
}
