package com.example.eventlottery;

import java.time.LocalDateTime;

/**
 * THis class records a invitation
 */
public class Invitation {
    public enum Status {
        PENDING,
        ACCEPTED,
        DECLINED
    }

    private Status status;
    private LocalDateTime CreateTime;
    private LocalDateTime ResponseTime;
    private String reason;
    private EntrantProfile entrant;
    private Event event;

    /**
     * Constructor
     * @param entrant:
     * @param event:
     */
    public Invitation(EntrantProfile entrant, Event event) {
        this.entrant = entrant;
        this.event = event;
        this.status = Status.PENDING;
        this.CreateTime = LocalDateTime.now();
        this.ResponseTime = null;
        this.reason = null;
        //NotificationService place holder
    }

    /**
     * A user accept an invitation
     * @param reason: optional reason
     */
    public void accept(String reason){
        this.status = Status.ACCEPTED;
        this. reason = reason;
        this.ResponseTime = LocalDateTime.now();
    }

    /**
     * A user decline an invitation
     * @param reason: optional reason
     */
    public void decline (String reason){
        this.status = Status.DECLINED;
        this.reason = reason;
        this.ResponseTime = LocalDateTime.now();
    }

    /**
     * getter for event
     * @return event object
     */
    public Event getEvent() {
        return event;
    }

    /**
     * getter for status
     * @return status object
     */
    public Status getStatus() {
        return status;
    }
}
