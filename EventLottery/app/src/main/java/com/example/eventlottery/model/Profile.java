package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores information for the profile of a user.
 * Holds functionalities for updating the profile and storing unique device ID.
 */
public class Profile {
    public enum Role {
        USER,
        ADMIN
    }

    private String deviceID;                     // Unique Device ID
    private String name;
    private String email;
    private String phone;
    private final Role role = Role.USER;        // Default role is USER
    private final List<Event> historyEvents = new ArrayList<>(); // For entrants
    private final List<Event> ownedEvents = new ArrayList<>();   // For organizers
    private final String preferences = "";      // Placeholder for user preferences

    /**
     * Constructor for minimal profile
     * @param deviceID unique device ID
     * @param name user name
     * @param email user email
     */
    public Profile(String deviceID, String name, String email) {
        this(deviceID, name, email, null);
    }

    /**
     * Full constructor with phone
     * @param deviceID unique device ID
     * @param name user name
     * @param email user email
     * @param phone user phone
     */
    public Profile(String deviceID, String name, String email, String phone) {
        this.deviceID = deviceID;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Updates the profile's personal info
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

    // ===== Device ID =====
    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    // ===== Other getters and setters =====
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Event> getHistoryEvents() {
        return historyEvents;
    }

    public List<Event> getOwnedEvents() {
        return ownedEvents;
    }

    public String getPreferences() {
        return preferences;
    }

    public Role getRole() {
        return role;
    }
}