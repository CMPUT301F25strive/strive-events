package com.example.eventlottery.data;

import com.example.eventlottery.model.Profile;
import java.util.List;

/**
 * Repository interface for managing user profiles and authentication.
 * Uses Firebase Authentication for secure login/registration,
 * and Firestore for storing additional user data (name, phone, etc.).
 */
public interface ProfileRepository {

    // ===== Firestore operations =====
    void findUserById(String id, ProfileCallback callback);
    void saveUser(Profile profile, ProfileCallback callback);
    void deleteUser(String id, ProfileCallback callback);
    List<Profile> findUsersByRole(Profile.Role role);

    // ===== Firebase Auth operations =====
    /**
     * Asynchronously checks if a user with this email already exists in Firebase Auth.
     */
    void userExists(String email, UserExistsCallback callback);

    /**
     * Attempts to log in a user securely with Firebase Auth.
     */
    void login(String email, String password, LoginCallback callback);

    /**
     * Registers a new user securely via Firebase Auth,
     * then stores additional info (phone, name, deviceID) in Firestore.
     */
    void register(String email, String password, String phone, String name, String deviceID, RegisterCallback callback);

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