package com.example.eventlottery.model;

import android.Manifest;
import android.content.Context;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.common.returnsreceiver.qual.This;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Coordinates notifying lottery winners and updating their RSVP status.
 * The service pulls information from the repositories, then delegates to a
 * {@link PushNotificationService} so the UI layer can stay unaware of delivery details.
 **/

public class InvitationService {

    private final FirebaseFirestore firestore;
    private final PushNotificationService push;

    public InvitationService(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.push = new PushNotificationService(context);
    }

    /**
     * This methods sets up the automatic notification sent to users who got invited to their specific event
     * @param deviceIds list of all invited device ids
     * @param senderId the event organizer
     * @param eventTitle the event title
     */
    public void sendWinnerInvitations(List<String> deviceIds, String senderId, String eventTitle) {
        String message = "Congratulations! You have been invited to " + eventTitle + "! Please accept or decline the invitation.";

        for (String deviceId : deviceIds) {
            push.sendNotification(senderId, deviceId, message, true);
        }
    }

    /**
     * This methods sets up the automatic notification sent to users who got invited to their specific event
     * @param deviceIds list of all invited device ids
     * @param senderId the event organizer
     * @param eventTitle the event title
     */
    public void sendLoserInvitations(List<String> deviceIds, String senderId, String eventTitle) {
        String message = "Unfortunately, You didn't not get invited to " + eventTitle + "! Better luck next time!";

        for (String deviceId : deviceIds) {
            push.sendNotification(senderId, deviceId, message, true);
        }
    }
}

