package com.example.eventlottery.data;

import com.example.eventlottery.model.Profile;

import java.util.List;

/**
 * This class holds CRUD for entrant and organizer profiles.
 * This provides search/browse for admin.
 */
public interface ProfileRepository {
    // Unified methods
    Profile findUserById(String id);
    void saveUser(Profile profile);
    void deleteUser(String id);

    // TODO: For admin browse and search

    /**
     * This searches the desired users
     * @param role: the role of the user
     * @return: a list of users categorized by their role
     */
    List<Profile> findUsersByRole(Profile.Role role);
    }