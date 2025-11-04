package com.example.eventlottery;

import java.util.List;

/**
 * This class functions as a event management, to add/edit events, generates qr code, assign images.
 */
public class EventController {
    private EventRepository eventRepository;
    private QRCodeService qrCodeService;
    private ImageStorage imageStorage;

    /**
     * constructor of the class
     * @param eventRepository: list of events
     * @param qrCodeService: class to generates qr code
     * @param imageStorage: class to store images
     */
    public EventController(EventRepository eventRepository, QRCodeService qrCodeService,  ImageStorage imageStorage) {
        this.eventRepository = eventRepository;
        this.qrCodeService = qrCodeService;
        this.imageStorage = imageStorage;
    }

    /**
     * This class creates a event given the information
     * @param eventID:
     * @param name:
     * @param description:
     * @param venue:
     * @param capacity:
     * @param price:
     * @param organizerID:
     * @param posterID:
     * @param geoRequired:
     * @return event object itself
     */
    public Event createEvent(int eventID, String name, String description, String venue,
                             int capacity, float price, int organizerID, int posterID, boolean geoRequired, WaitingList waitList) {

        Event event = new Event(eventID, name, description, venue, capacity, price, organizerID, posterID, geoRequired, waitList);
        eventRepository.add(event);
        return event;
    }

    /**
     * this functions updates the event object by checking if there's any difference between exist information and the new information about the events
     * @param event:
     * @param newName:
     * @param newDescription:
     * @param newCapacity:
     * @param newVenue:
     * @param newPrice:
     * @param newPosterID:
     */
    public void updateEvent(Event event, String newName, String newDescription, Integer newCapacity, String newVenue, Float newPrice, Integer newPosterID, WaitingList newWaitList) {
        if (newName != null) event.setName(newName);
        if (newDescription != null) event.setDescription(newDescription);
        if (newCapacity != null) event.setCapacity(newCapacity);
        if (newVenue != null) event.setVenue(newVenue);
        if (newPrice != null) event.setPrice(newPrice);
        if (newPosterID != null) event.setPosterID(newPosterID);
        if (newWaitList != null) event.setWaitingList(newWaitList);

        eventRepository.update(event);
    }

    /**
     * generates qr code given the event
     * @param event: event object
     * @return a qr code image
     */
    public String generateEventQRCode(Event event) {
        return qrCodeService.generateQRCode(event.getEventID());
    }

    /**
     * It gets all the events in the event repository
     * @return a list of events
     */
    public List<Event> getAllEvents() {
        return eventRepository.getEvents();
    }

    //place holder for filter events
}