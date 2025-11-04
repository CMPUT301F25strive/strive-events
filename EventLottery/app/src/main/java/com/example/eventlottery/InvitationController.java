package com.example.eventlottery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class manage acceptance, trigger replacement draws
 */
public class InvitationController {
    //private LotteryController lotteryController;
    private Event event;
    //private NotificationService notificationService;
    private List<Invitation> Invitations = new ArrayList<>();

    /**
     * constructor for invitation controller
     * @param lotteryController:
     * @param event:
     * @param notificationService:
     */

    public InvitationController(LotteryController lotteryController,
                                Event event,
                                NotificationService notificationService) {
        this.lotteryController = lotteryController;
        this.event = event;
        this.notificationService = notificationService;
    }

    /**
     * This class as a invitation to the event
     * @param invitation: invitation
     */
    public void addInvitation(Invitation invitation) {
        Invitations.add(invitation);
        eventRepository.saveInvitation(invitation);
    }

    /**
     * THis class accepts a invitation and add this information to the event
     * @param invitation invitation object
     * @param reason optional reasons
     */
    public void acceptInvitation(Invitation invitation, String reason) {

        invitation.accept(reason);
    }
    /**
     * THis class decline a invitation and add this information to the event, and call a replacement draw
     * @param invitation invitation object
     * @param reason optional reasons for rejecting
     */
    public void declineInvitation(Invitation invitation, String reason) {

        invitation.decline(reason);
        lotteryController.triggerReplacementDraw(invitation.getEvent());
        notificationService.notifyDecline(invitation);
    }

    /**
     * THis function checks if a deadline has passed for the invitations
     * @param invitation:
     * @return boolean value for checking deadline
     */
    private boolean CheckDeadline(Invitation invitation) {
        Event event = invitation.getEvent();
        LocalDateTime deadline = event.getInvitationDeadline();
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Sort all the invitations by status
     * @param status: Accept, pending or decline
     * @return the list of invitations for the specific status
     */
    public List<Invitation> getInvitationsByStatus(Invitation.Status status) {
        return Invitations.stream()
                .filter(inv -> inv.getStatus() == status)
                .collect(Collectors.toList());
    }

}
