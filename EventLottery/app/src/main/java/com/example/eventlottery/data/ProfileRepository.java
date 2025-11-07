package com.example.eventlottery.data;

import androidx.lifecycle.LiveData;

import com.example.eventlottery.model.Profile;
import java.util.List;

/**
 * Repository interface for managing user profiles and authentication.
 * Login/registration is based solely on deviceID for auto-detected authentication.
 */
public interface ProfileRepository {

    // ===== Firestore operations =====
    void findUserById(String deviceID, ProfileCallback callback);
    Profile findUserById(String deviceID);
    void saveUser(Profile profile, ProfileCallback callback);
    void deleteUser(String deviceID, ProfileCallback callback);

    void saveUser(Profile profile);

    void deleteUser(String id);

    List<Profile> findUsersByRole(Profile.Role role);

    LiveData<List<Profile>> observeProfiles();

    // ===== DeviceID-based Auth operations =====
    /**
     * Checks if a user with this email already exists.
     */
    void userExists(String email, UserExistsCallback callback);

    /**
     * Attempts to log in a user using email + deviceID.
     * Fails if deviceID does not match the stored deviceID for that email.
     */
    void login(String email, String deviceID, LoginCallback callback);

    /**
     * Registers a new user with email, name, phone, and deviceID.
     */
    void register(String email, String phone, String name, String deviceID, RegisterCallback callback);

    // ===== Callback Interfaces =====
    interface ProfileCallback {
        void onSuccess(Profile profile);
        void onDeleted();
        void onError(String message);
    }

    interface UserExistsCallback {
        void onResult(boolean exists);
    }

    interface LoginCallback {
        void onResult(boolean success, String message);
    }

    interface RegisterCallback {
        void onResult(boolean success, String message);
    }
}