package com.example.eventlottery.data;

import androidx.lifecycle.LiveData;

import com.example.eventlottery.model.Profile;
import java.util.List;

/**
 * Repository interface for managing user profiles and authentication.
 * Login/registration is based on deviceID for fast auto-login.
 */
public interface ProfileRepository {

    // ===== Firestore operations =====
    void findUserById(String deviceID, ProfileCallback callback); // async fetch
    void saveUser(Profile profile, ProfileCallback callback);     // async save
    void deleteUser(String deviceID, ProfileCallback callback);   // async delete

    // Optional silent operations (not used for UI)
    void saveUser(Profile profile);
    void deleteUser(String deviceID);

    // Role-based fetching (optional, can return empty list)
    List<Profile> findUsersByRole(Profile.Role role);

    LiveData<List<Profile>> observeProfiles();

    // ===== DeviceID-based Auth operations =====
    void userExists(String email, UserExistsCallback callback);
    void login(String email, String deviceID, LoginCallback callback);
    void register(String email, String phone, String name, String deviceID, RegisterCallback callback);

    // ===== Callback Interfaces =====
    interface ProfileCallback {
        void onSuccess(Profile profile); // called when fetch/save succeeds
        void onDeleted();                // called when deletion succeeds
        void onError(String message);    // called on failure
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
