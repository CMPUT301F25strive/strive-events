/*
package com.example.eventlottery.model;

import android.Manifest;
import android.content.Context;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;

import java.util.List;
import java.util.NoSuchElementException;

*/
/**
 * Coordinates notifying lottery winners and updating their RSVP status.
 * The service pulls information from the repositories, then delegates to a
 * {@link PushNotificationService} so the UI layer can stay unaware of delivery details.
 *//*

public class InvitationService {
    private final EventRepository eventRepository;

    private final ProfileRepository profileRepository;

    private final PushNotificationService notificationService;

    */
/**
     * Creates a new service that can look up events and users before sending notifications.
     *
     * @param eventRepository   repository used to fetch event metadata
     * @param profileRepository repository used to read profile information
     * @param context           Android context needed for push notifications
     *//*

    public InvitationService(EventRepository eventRepository, ProfileRepository profileRepository, Context context) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.notificationService = new PushNotificationService(context);
    }

    */
/**
     * Sends invitations to all winners by looking up their profile and the related event.
     *
     * @param winners list of device IDs that were drawn from the waiting list
     * @param eventId identifier of the event the invitation belongs to
     *//*

    public void sendInvitations(List<String> winners, String eventId) {
        for (String deviceId : winners) {
            Profile winner = profileRepository.findUserById(deviceId);
            Event event = eventRepository.findEventById(eventId);
            if (winner != null) {
                String title = "You've been invited to join " + event.getTitle() + "!";
                String message = "Congratulations " + winner.getName() + "for winning a spot! Check the app for more details.";
                notificationService.sendNotification(title, message);
            } else {
                throw new NoSuchElementException("Winner doesn't not exist in repository");
            }
        }
    }

    */
/**
     * Accepts an invitation for a specific user and event.
     * This updates the user's status in the repository or database.
     *
     * @param deviceId the ID of the user accepting the invitation
     * @param eventId  the ID of the event tied to the waiting list winners
     *//*

    public void acceptInvitation(String deviceId, String eventId) {
        Profile profile = profileRepository.findUserById(deviceId);
        Event event = eventRepository.findEventById(eventId);
        if (profile != null) {
            // Status becomes accepted
            event.joinAttendeesList(deviceId);
            System.out.println(profile.getName() + " has accepted the invitation to " + event.getTitle());
        } else {
            throw new IllegalArgumentException("User not found in repository");
        }
    }


    */
/**
     * Declines an invitation for a specific user and event.
     * This removes or updates the user's invitation status.
     *
     * @param deviceId the ID of the user declining the invitation
     * @param eventId  the ID of the event tied to the waiting list winners
     *//*

    public void declineInvitation(String deviceId, String eventId) {
        Profile profile = profileRepository.findUserById(deviceId);
        Event event = eventRepository.findEventById(eventId);
        if (profile != null) {
            event.leaveWaitingList(eventId); // Does the entrant leave the waiting list if they decline?
            // Status becomes cancelled
            System.out.println(profile.getName() + " has declined the invitation to " + event.getTitle());
        } else {
            throw new IllegalArgumentException("User not found in repository");
        }
    }
}
*/
