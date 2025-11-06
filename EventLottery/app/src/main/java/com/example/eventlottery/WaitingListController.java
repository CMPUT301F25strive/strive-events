/*
package com.example.eventlottery;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.WaitingList;

import java.util.List;

*/
/**
 * This class handles the joining and leaving of entrants regarding the waiting list
 * get the number of entrants in the waiting list
 * displays the map of the location of all the entrants in the waiting list
 *//*

public class WaitingListController {
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;

    */
/**
     * This is a constructor of WaitingListController with the given Profile and Event Repository
     * @param eventRepository: the repository used to manage the events
     * @param profileRepository: the repository used to manage the profiles
     *//*


    public WaitingListController(EventRepository eventRepository, ProfileRepository profileRepository) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
    }

    */
/**
     * This methods adds entrants based on their entrantID to the waiting list to the event specified by eventID
     * @param eventID: ID of the event
     * @param deviceID: ID of the user's device
     *//*

    public void joinWaitingList(String eventID, String deviceID) {
        Event event = eventRepository.getEvent(eventID);
        EntrantProfile entrant = profileRepository.findEntrantById(entrantID);
        WaitingList waitingList = event.getWaitingList(); // might have to add the method getWaitingList() in EventRepository since it shows error without it

        if (waitingList.getEntrantCount() >= event.getCapacity()) {
            throw new IllegalArgumentException(); //change this exception
        }

        waitingList.joinList(entrant);
        profileRepository.saveEntrant(entrant);
    }

    */
/**
     * This methods removes entrants based on their entrantID from the waiting list to the event specified by eventID
     * @param eventID: ID of the event
     * @param entrantID: ID of the entrant
     *//*

    public void leaveWaitingList(String eventID, String entrantID) {
        Event event = eventRepository.getEvent(eventID);
        EntrantProfile entrant = profileRepository.findEntrantById(entrantID);
        WaitingList waitingList = event.getWaitingList();

        waitingList.leaveList(entrant);
    }

    */
/**
     * This methods returns the number of entrants that are in the waiting list
     * @param eventID: ID of the event
     * @return int: number of entrants in the waiting list
     *//*

    public int countEntrants(String eventID) {
        Event event = eventRepository.getEvent(eventID);
        return event.getWaitingList().getEntrantCount();
    }

    */
/**
     * This methods displays the map of the locations of all the entrants in the waiting list
     * @param eventID: ID of the event
     *//*

    public void showMapView(String eventID) {
        Event event = eventRepository.getEvent(eventID);
        //... // will complete after implementation of the GeoService
    }

}
*/
