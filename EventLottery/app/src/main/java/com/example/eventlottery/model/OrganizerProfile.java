package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores information for the profile of an organizer.
 *
 */
public class OrganizerProfile {
    private String deviceId;
    private String name;
    private String email;
    private String phone;
    private List<Event> ownedEvents = new ArrayList<>();    //
    private String permissions = "organizer";   //
    private boolean deleted = false;    //

    /**
     * This constructor delegates to the full constructor with an empty phone value
     * @param name: name of user
     * @param email: email of user
     */
    public OrganizerProfile(String name, String email) {
        this(name, email, null); // delegate to full constructor
    }

    /**
     * This is the full constructor with necessary personal info for a profile
     * @param name: name of user
     * @param email: email of user
     * @param phone: phone number of user
     */
    public OrganizerProfile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
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

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}

