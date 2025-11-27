package com.example.eventlottery;

import android.location.Location;

import androidx.annotation.Nullable;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Event;

/**
 * This class handles joining/leaving entrants on waiting lists,
 * fully integrated with Firebase for live updates and optional geolocation.
 */
public class WaitingListController {
    private final EventRepository eventRepository;
    private final ProfileRepository profileRepository;

    /**
     * This is a constructor of WaitingListController with the given Profile and Event Repository
     * @param eventRepository repository for managing events (interface)
     * @param profileRepository repository for managing user profiles
     */
    public WaitingListController(EventRepository eventRepository, ProfileRepository profileRepository) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * This methods adds entrants based on their entrantID to the waiting list to the event specified by eventID
     * Updates both waitingList and userLocations in Firestore.
     * @param eventID event ID
     * @param userID device/user ID
     * @param location optional Location object
     */
    public void joinWaitingList(String eventID, String userID, @Nullable Location location) {
        Event event = eventRepository.findEventById(eventID);
        if (event == null) {
            // Event not in local cache yet
            System.out.println("joinWaitingList: event not loaded for id=" + eventID);
            return;
        }

        // Only allow joining within registration period and when the waiting list has spots
        if (!event.isRegOpen() || event.isWaitingListFull()) {
            System.out.println("Cannot join waiting list");
            return;
        }

        double lat = location != null ? location.getLatitude() : Double.NaN;
        double lon = location != null ? location.getLongitude() : Double.NaN;

        // Add user to waiting list if not already present
        if (!event.isOnWaitingList(userID)) {
            if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                event.joinWaitingList(userID, lat, lon);
            } else {
                event.joinWaitingList(userID);
            }

            // Update waiting list via repository
            eventRepository.updateWaitingList(eventID, event.getWaitingList());

            // Update user locations if applicable
            if (!Double.isNaN(lat) && !Double.isNaN(lon) && event.isGeolocationEnabled()) {
                eventRepository.updateUserLocations(eventID, event.getUserLocations());
            }
        }
    }

    /**
     * Overloaded join without location
     * @param eventID event ID
     * @param userID device/user ID
     */
    public void joinWaitingList(String eventID, String userID) {
        joinWaitingList(eventID, userID, null);
    }

    /**
     * This method removes entrants from waiting list and optionally remove stored location.
     * Updates both waitingList and userLocations in Firestore.
     * @param eventID event ID
     * @param userID device/user ID
     * @param removeLocation true to remove geolocation from Firestore
     */
    public void leaveWaitingList(String eventID, String userID, boolean removeLocation) {
        Event event = eventRepository.findEventById(eventID);
        if (event == null) {
            System.out.println("leaveWaitingList: event not loaded for id=" + eventID);
            return;
        }

        if (!event.isOnWaitingList(userID)) return;

        // Remove from waiting list
        event.leaveWaitingList(userID);

        // Remove user location if requested
        if (removeLocation && event.isGeolocationEnabled()) {
            event.removeUserLocation(userID);
        }

        // Update waiting list via repository
        eventRepository.updateWaitingList(eventID, event.getWaitingList());

        // Update user locations via repository if needed
        if (removeLocation && event.isGeolocationEnabled()) {
            eventRepository.updateUserLocations(eventID, event.getUserLocations());
        }
    }

    /**
     * Overloaded leave with default removeLocation = true
     * @param eventID event ID
     * @param userID device/user ID
     */
    public void leaveWaitingList(String eventID, String userID) {
        leaveWaitingList(eventID, userID, true);
    }
}