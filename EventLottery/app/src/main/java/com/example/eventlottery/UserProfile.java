package com.example.eventlottery;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores information for the profile of a user
 * This holds the functionalities for updating and deleting profile.
 */
public class UserProfile {
    private String deviceID;    // unique key
    private String name;
    private String email;
    private String phone;
    private List<Event> historyEvents = new ArrayList<>();
    private List<Event> ownedEvents = new ArrayList<>();
    private String preferences = ""; //
    private boolean deleted = false;

    /**
     * This constructor delegates to the full constructor with an empty phone value
     * @param name: name of user
     * @param email: email of user
     */
    public UserProfile(String name, String email) {
        this(name, email, null); // delegate to full constructor
    }

    /**
     * This is the full constructor with necessary personal info for a profile
     * @param name: name of user
     * @param email: email of user
     * @param phone: phone number of user
     */
    public UserProfile(String name, String email, String phone) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    /**
     * This is the full constructor with necessary personal info for a profile
     * @param deviceID: the ID of the user's device
     * @param name: name of user
     * @param email: email of user
     * @param phone: phone number of user
     */
    public UserProfile(String deviceID, String name, String email, String phone) {
        this.deviceID = deviceID;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    /**
     * This method updates the current profile with given parameter values.
     * @param name: new name
     * @param email: new email
     * @param phone: new phone number
     */
    public void updatePersonalInfo(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * This method returns the user's device ID.
     * @return the value of the unique device ID
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * This method returns user's name
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns the user's email
     * @return the value of email
     */
    public String getEmail() {
        return email;
    }

    /**
     * This method returns the user's phone number
     * @return the value of phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * This method returns the list of history events from user
     * @return the list of event references
     */
    public List<Event> getHistoryEvents() {
        return historyEvents;
    }

    /**
     * This method returns the preferences of the user
     * @return the value of preference
     */
    public String getPreferences() {
        return preferences;
    }

    /**
     * This method returns whether this profile is deleted
     * @return true if the profile is deleted, false otherwise
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * This method notifies the profile repository that the profile is deleted
     */
    public void deleteProfile() {
        this.deleted = true;
    }

}

