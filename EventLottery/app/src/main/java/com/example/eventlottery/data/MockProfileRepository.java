package com.example.eventlottery.data;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A mock version of FirebaseEventRepository for local unit testing.
 * It keeps data in memory and does not use Firebase.
 */
public class MockProfileRepository implements ProfileRepository {

    private final List<Profile> users = new ArrayList<>();


    /**
     * @param deviceId
     * @return
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

    /**
     * @param profile
     */
    @Override
    public void saveUser(Profile profile) {
        users.add(profile);
    }

    /**
     * @param id
     */
    @Override
    public void deleteUser(String id) {
        users.removeIf(profile -> id.equals(profile.getDeviceID()));
    }

    /**
     * @param role: the role of the user
     * @return
     */
    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        return Collections.emptyList();
    }
}

