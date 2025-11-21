
package com.example.eventlottery;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Event;

import org.checkerframework.common.returnsreceiver.qual.This;

import java.util.List;

/**
 * This class handles the joining and leaving of entrants regarding the waiting list
 * get the number of entrants in the waiting list
 * displays the map of the location of all the entrants in the waiting list
 */
public class WaitingListController {
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;

    /**
     * This is a constructor of WaitingListController with the given Profile and Event Repository
     * @param eventRepository: the repository used to manage the events
     * @param profileRepository: the repository used to manage the profiles
     */
    public WaitingListController(EventRepository eventRepository, ProfileRepository profileRepository) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * This methods adds entrants based on their entrantID to the waiting list to the event specified by eventID
     * @param eventID: ID of the event
     * @param userID: ID of the user's device
     */
    public void joinWaitingList(String eventID, String userID) {
        Event event = eventRepository.findEventById(eventID);
        if (event == null) {
            // Event not in local cache yet – avoid crashing
            System.out.println("joinWaitingList: event not loaded for id=" + eventID);
            return;
        }

        // Only allow joining within registration period and when the waiting list has spots
        if (!event.isRegOpen() || event.isWaitingListFull()) {
            System.out.println("Cannot join waiting list");
            return;
        }

        // If the user hasn't joined the waiting list, join it
        if (!event.isOnWaitingList(userID)) {
            event.joinWaitingList(userID);
            // Update Firebase immediately
            eventRepository.updateWaitingList(eventID, event.getWaitingList());
        }
    }

    /**
     * This methods removes entrants based on their entrantID from the waiting list to the event specified by eventID
     * @param eventID: the unique key for events
     * @param userID: the unique key for users
     */
    public void leaveWaitingList(String eventID, String userID) {
        Event event = eventRepository.findEventById(eventID);
        if (event == null) {
            System.out.println("leaveWaitingList: event not loaded for id=" + eventID);
            return;
        }

        if (event.isOnWaitingList(userID)) {
            event.leaveWaitingList(userID);
            eventRepository.updateWaitingList(eventID, event.getWaitingList());
        }
    }
}