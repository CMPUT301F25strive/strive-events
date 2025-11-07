package com.example.eventlottery.data;

import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A mock version of FirebaseRepository Repository for local unit testing.
 * It keeps data in memory and does not use Firebase.
 */
public class MockProfileRepository implements ProfileRepository {

    private final List<Profile> users = new ArrayList<>();


    /**
     * This method finds the user's profile based on their unique device id
     * @param deviceId: unique device id specific to user
     * @return u: user that matches the inputted device id
     */
    @Override
    public Profile findUserById(String deviceId) {
        for (Profile u : users) {
            if (Objects.equals(u.getDeviceID(), deviceId)) {
                return u;
            }
        }
        return null;
    }

    public void findUserById(String deviceID, ProfileCallback callback) {}
    public void saveUser(Profile profile, ProfileCallback callback) {}
    public void deleteUser(String deviceID, ProfileCallback callback) {}
    public void userExists(String email, UserExistsCallback callback) {}

    @Override
    public void login(String email, String deviceID, LoginCallback callback) {
    }

    @Override
    public void register(String email, String phone, String name, String deviceID, RegisterCallback callback) {

    }

    /**
     * This methods saves the user's profile into the firebase repository for profiles
     * @param profile: profile that is desired to be saved in the firebase repository
     */
    @Override
    public void saveUser(Profile profile) {
        users.add(profile);
    }

    /**
     * This method deletes the user's profile from the firebase repository
     * @param id: ID of the profile that is to be deleted
     */
    @Override
    public void deleteUser(String id) {
        users.removeIf(profile -> id.equals(profile.getDeviceID()));
    }

    /**
     * This methods filters the user's profile based off their roles
     * @param role: the role of the user
     * @return list: list of all the profiles that have the desired roll
     */
    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        return Collections.emptyList();
    }
}

