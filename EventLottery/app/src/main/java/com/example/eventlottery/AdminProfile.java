package com.example.eventlottery;

/**
 * This class creates a profile for Admin
 */
public class AdminProfile {
    private String name;
    private String email;
    private String phone;
    private String permissions = "Admin";

    /**
     * This constructor creates a admin profile with name and email, phone is optional
     * @param name: name of user
     * @param email: email of user
     * @param phone: phone number of user
     */
    public AdminProfile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone; // phone number is null if user doesn't enter anything
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
     * This method returns the name of the admin.
     * @return name of admin.
     */
    public String getName(){
        return name;
    }

    /**
     * This method returns the email of the admin.
     * @return email of admin.
     */
    public String getEmail(){
        return email;
    }

    /**
     * This method returns the phone number of the admin.
     * @return phone of admin.
     */
    public String getPhone(){
        return phone;
    }

}
