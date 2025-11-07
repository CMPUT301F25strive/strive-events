/*
package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

*/
/**
 * This is a class representing a Waiting List object
 *//*

public class WaitingList {
    private List<Profile> waitList = new ArrayList<>();

    */
/**
     * This methods constructs a WaitingList object
     *//*

    public WaitingList() {
        this.waitList = new ArrayList<>();
    }

    */
/**
     * This adds an entrant to the waiting list
     * @param entrant: the entrant profile that is joining the list
     *//*

    public void joinList(Profile entrant) {
        if (entrant == null) {
            throw new IllegalArgumentException("Entrant cannot be null");
        }
        
        // Check if entrant is already in the list
        if (!contains(entrant)) {
            waitList.add(entrant);
        }
    }

    */
/**
     * This removes an entrant from the waiting list
     * @param entrant: the entrant profile that is removed from the list
     *//*

    public void leaveList(Profile entrant) {
        if (entrant == null) return;
        waitList.remove(entrant);
    }

    */
/**
     * This returns the number of entrants that joined the waiting list
     * @return waitList.size(): the amount of entrants in the waiting list
     *//*

    public int getProfileCount() {
        return waitList.size();
    }

    */
/**
     *
     * Gets the current waiting list
     * @return list of profiles in waiting list
     *//*

    public List<Profile> getWaitList() {
        return new ArrayList<>(waitList);
    }

    */
/**
     * Checks if an entrant is in the waiting list
     * @param entrant the entrant to check
     * @return true if contains, false otherwise
     *//*

    public boolean contains(Profile entrant) {
        if (entrant == null) return false;
        return waitList.contains(entrant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Check instance type
        if (!(o instanceof Profile)) return false;
        
        Profile other = (Profile) o;
        return Objects.equals(this.deviceID, other.deviceID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceID);
    }
}*/
