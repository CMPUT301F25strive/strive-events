package com.example.eventlottery.model;

import android.content.Context;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lightweight test double for {@link InvitationService} that prints notifications
 * instead of interacting with the Android notification manager.
 */
public class MockInvitationService {
    private final EventRepository eventRepository;

    private final ProfileRepository profileRepository;

    private final MockNotificationService notificationService;

    /**
     * This is a constructor of MockInvitationService with the given Profile Repository for testing
     *
     * @param eventRepository   repository used to fetch event metadata
     * @param profileRepository the repository used to manage the profiles
     * @param context           context required only because the production service depends on it
     */
    public MockInvitationService(EventRepository eventRepository, ProfileRepository profileRepository, Context context) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.notificationService = new MockNotificationService(context);
    }

    /**
     * This is a method that takes the list of winners and sends an invitation to them through notifications
     *
     * @param winners list of unique ids that are the winners from the waiting list
     * @param eventId title of the event tied to the waiting list winners
     */
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

    /**
     * Accepts an invitation for a specific user and event.
     * This updates the user's status in the repository or database.
     *
     * @param deviceId the ID of the user accepting the invitation
     * @param eventId  the ID of the event tied to the waiting list winners
     */
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


    /**
     * Declines an invitation for a specific user and event.
     * This removes or updates the user's invitation status.
     *
     * @param deviceId the ID of the user declining the invitation
     * @param eventId  the ID of the event tied to the waiting list winners
     */
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
