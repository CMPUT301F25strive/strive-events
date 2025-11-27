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
        ORGANIZER,
        ADMIN
    }

    private String deviceID;                     // Unique Device ID
    private String name;
    private String email;
    private String phone;
    private Role role = Role.ORGANIZER;        // Default role is ORGANIZER
    private boolean getNotifications = true; //Default is true
    private final List<Event> historyEvents = new ArrayList<>(); // For entrants
    private final List<Event> ownedEvents = new ArrayList<>();   // For organizers
    private final String preferences = "";      // Placeholder for user preferences

    /**
     * Constructor for minimal profile
     * @param deviceID unique device ID
     * @param name user name
     * @param email user email
     * @param getNotifications setting of if the user wants notifications or not
     */
    public Profile(String deviceID, String name, String email, boolean getNotifications) {
        this(deviceID, name, email, null, getNotifications); // delegate to full constructor
    }

    /**
     * Full constructor with phone
     * @param deviceID unique device ID
     * @param name user name
     * @param email user email
     * @param phone user phone
     * @param getNotifications setting of if the user wants notifications or not
     */
    public Profile(String deviceID, String name, String email, String phone, boolean getNotifications) {
        this(deviceID, name, email, phone, Role.ORGANIZER, getNotifications);
    }

    /**
     * Constructor with role
     * @param deviceID unique device ID
     * @param name user name
     * @param email user email
     * @param phone user phone
     * @param role the role of the user
     * @param getNotifications setting of if the user wants notifications or not
     */
    public Profile(String deviceID, String name, String email, String phone, Role role, boolean getNotifications) {
        this.deviceID = deviceID;   // From DeviceIdentityService
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.getNotifications = getNotifications;
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

    /**
     * @return true if the profile represents an administrator
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * @return true if the profile can access organizer capabilities
     */
    public boolean isOrganizer() {
        return role == Role.ORGANIZER || role == Role.ADMIN;
    }

    /**
     * This method returns the user's device ID.
     * @return the value of the unique device ID
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * This method sets user's device ID.
     * @param deviceID  the value of the unique device ID
     */
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

    /**
     * This method sets user's name
     * @param name  the value of name
     */
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

    /**
     * This method sets the user's email
     * @param email  the value of email
     */
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

    /**
     * This method sets the user's phone number
     * @param phone  the value of phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * This method returns the user's notification settings
     * @return the boolean of the notification settings
     */
    public boolean getNotificationSettings() {
        return getNotifications;
    }

    /**
     * This method sets the user's notification settings
     * @param getNotifications  the boolean of the notification settings
     */
    public void setNotificationSettings(boolean getNotifications) {
        this.getNotifications = getNotifications;
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
     * This method returns the role of the user
     * @return role of user
     */
    public Role getRole() { return role; }

    /**
     * This method sets the role of the user
     * @param role : role of user
     */
    public void setRole(Role role) {
        this.role = role != null ? role : Role.ORGANIZER;
    }
}
