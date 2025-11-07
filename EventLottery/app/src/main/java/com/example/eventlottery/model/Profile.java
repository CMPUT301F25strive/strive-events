package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores information for the profile of a user.
 * This holds the functionalities for updating the profile.
 */
public class Profile {
    public enum Role {
        USER,
        ADMIN
    }

    private String deviceID;
    private String name;
    private String email;
    private String phone;
    private Role role = Role.USER;   // default to entrant, actual value comes from Firebase
    private final List<Event> historyEvents = new ArrayList<>();  // for entrants
    private final List<Event> ownedEvents = new ArrayList<>();    // for organizers
    private final String preferences = "";    // unsure

    /**
     * This constructor delegates to the full constructor with an empty phone value
     * @param name: name of user
     * @param email: email of user
     */
    public Profile(String deviceID, String name, String email) {
        this(deviceID, name, email, null); // delegate to full constructor
    }

    /**
     * This is the full constructor with necessary personal info for a profile
     * @param name: name of user
     * @param email: email of user
     * @param phone: phone number of user
     */
    public Profile(String deviceID, String name, String email, String phone) {
        this(deviceID, name, email, phone, Role.USER);
    }

    public Profile(String deviceID, String name, String email, String phone, Role role) {
        this.deviceID = deviceID;   // From DeviceIdentityService
        this.name = name;
        this.email = email;
        this.phone = phone;
        setRole(role);
    }

    /**
     * This method updates the current profile with given parameter values.
     * @param name: new name
     * @param email: new email
     * @param phone: new phone number
     */
    public void updatePersonalInfo(String name, String email, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        this.name = name.trim();
        this.email = email.trim();
        this.phone = phone != null ? phone.trim() : null;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * This method returns the user's device ID.
     * @return the value of the unique device ID
     */
    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * This method returns user's name
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method returns the user's email
     * @return the value of email
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * This method returns the user's phone number
     * @return the value of phone
     */
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Role getRole() { return role; }

    public void setRole(Role role) {
        this.role = role != null ? role : Role.USER;
    }
}
