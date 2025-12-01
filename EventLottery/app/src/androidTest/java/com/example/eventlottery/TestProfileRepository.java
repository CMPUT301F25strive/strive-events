package com.example.eventlottery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very small in-memory ProfileRepository used by instrumentation tests so they
 * don't need to reach Firebase.
 */
public class TestProfileRepository implements ProfileRepository {

    private final Map<String, Profile> profiles = new HashMap<>();
    private final MutableLiveData<List<Profile>> liveData = new MutableLiveData<>(new ArrayList<>());

    @Override
    public void findUserById(String deviceID, ProfileCallback callback) {
        Profile profile = profiles.get(deviceID);
        if (profile != null) {
            callback.onSuccess(profile);
        } else {
            callback.onDeleted();
        }
    }

    @Override
    public Profile findUserById(String deviceID) {
        return profiles.get(deviceID);
    }

    @Override
    public void saveUser(Profile profile, ProfileCallback callback) {
        saveUser(profile);
        callback.onSuccess(profile);
    }

    @Override
    public void deleteUser(String deviceID, ProfileCallback callback) {
        deleteUser(deviceID);
        callback.onDeleted();
    }

    @Override
    public void saveUser(Profile profile) {
        if (profile == null || profile.getDeviceID() == null) {
            return;
        }
        profiles.put(profile.getDeviceID(), profile);
        publish();
    }

    @Override
    public void deleteUser(String id) {
        profiles.remove(id);
        publish();
    }

    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        List<Profile> results = new ArrayList<>();
        for (Profile profile : profiles.values()) {
            if (profile.getRole() == role) {
                results.add(profile);
            }
        }
        return results;
    }

    @Override
    public LiveData<List<Profile>> observeProfiles() {
        return liveData;
    }

    @Override
    public void userExists(String email, UserExistsCallback callback) {
        boolean exists = profiles.values().stream()
                .anyMatch(profile -> email.equals(profile.getEmail()));
        callback.onResult(exists);
    }

    @Override
    public void login(String email, String deviceID, LoginCallback callback) {
        Profile profile = profiles.get(deviceID);
        boolean success = profile != null && email.equals(profile.getEmail());
        callback.onResult(success, success ? "ok" : "not found");
    }

    @Override
    public void register(String email, String phone, String name, String deviceID, RegisterCallback callback) {
        Profile profile = new Profile(deviceID, name, email, phone, true);
        saveUser(profile);
        callback.onResult(true, "registered");
    }

    private void publish() {
        liveData.postValue(new ArrayList<>(profiles.values()));
    }
}
