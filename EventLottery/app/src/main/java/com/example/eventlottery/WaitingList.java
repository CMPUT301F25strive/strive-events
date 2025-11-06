package com.example.eventlottery;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a class representing a Waiting List object
 */
public class WaitingList {
    private List<EntrantProfile> waitList = new ArrayList<>();

    /**
     * This methods constructs a WaitingList object
     * @param waitList: the list of entrants that joined the waiting list of the event
     */
    public WaitingList(List<EntrantProfile> waitList) {
        this.waitList= waitList;
    }

    /**
     * This adds an entrant to the waiting list
     * @param entrant: the entrant profile that is joining the list
     */
    public void joinList(EntrantProfile entrant) {
        if (!waitList.contains(entrant)){
            waitList.add(entrant);
        }
    }

    /**
     * This removes an entrant from the waiting list
     * @param entrant: the entrant profile that is removed from the list
     */
    public void leaveList(EntrantProfile entrant) {
        waitList.remove(entrant);
    }

    /**
     * This returns the number of entrants that joined the waiting list
     * @return waitList.size(): the amount of entrants in the waiting list
     */
    public int getEntrantCount() {
        return waitList.size();
    }

}
