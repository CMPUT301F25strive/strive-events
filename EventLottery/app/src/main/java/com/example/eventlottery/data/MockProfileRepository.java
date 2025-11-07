package com.example.eventlottery.data;

import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MockProfileRepository implements ProfileRepository {

    private final List<Profile> users = new ArrayList<>();

    // ===== Async version =====
    @Override
    public void findUserById(String deviceID, ProfileCallback callback) {
        for (Profile u : users) {
            if (Objects.equals(u.getDeviceID(), deviceID)) {
                callback.onSuccess(u);
                return;
            }
        }
        callback.onError("User not found");
    }

    // ===== Firestore-style methods =====
    @Override
    public void saveUser(Profile profile, ProfileCallback callback) {
        users.add(profile);
        callback.onSuccess(profile);
    }

    @Override
    public void deleteUser(String deviceID, ProfileCallback callback) {
        boolean removed = users.removeIf(profile -> deviceID.equals(profile.getDeviceID()));
        if (removed) callback.onDeleted();
        else callback.onError("User not found");
    }

    @Override
    public void userExists(String email, UserExistsCallback callback) {
        boolean exists = users.stream().anyMatch(u -> email.equals(u.getEmail()));
        callback.onResult(exists);
    }

    @Override
    public void login(String email, String deviceID, LoginCallback callback) {
        for (Profile u : users) {
            if (email.equals(u.getEmail()) && deviceID.equals(u.getDeviceID())) {
                callback.onResult(true, "Login successful");
                return;
            }
        }
        callback.onResult(false, "Login failed");
    }

    @Override
    public void register(String email, String phone, String name, String deviceID, RegisterCallback callback) {
        boolean exists = users.stream().anyMatch(u -> email.equals(u.getEmail()));
        if (exists) {
            callback.onResult(false, "User already exists");
            return;
        }
        Profile newProfile = new Profile(deviceID, name, email, phone);
        users.add(newProfile);
        callback.onResult(true, "Registration successful");
    }

    // ===== Synchronous versions =====
    @Override
    public void saveUser(Profile profile) {
        users.add(profile);
    }

    @Override
    public void deleteUser(String id) {
        users.removeIf(profile -> id.equals(profile.getDeviceID()));
    }

    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        return Collections.emptyList();
    }
}