package com.example.eventlottery.data;

import androidx.annotation.NonNull;

import com.example.eventlottery.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FirebaseProfileRepository manages user profiles in Firestore.
 * Uses deviceID as the login key (no password/email authentication needed).
 */
public class FirebaseProfileRepository implements ProfileRepository {

    private final FirebaseFirestore db;
    private final CollectionReference usersRef;

    public FirebaseProfileRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    // ===== Firestore operations =====

    @Override
    public void findUserById(String deviceID, @NonNull ProfileCallback callback) {
        usersRef.document(deviceID).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Profile profile = new Profile(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("email"),
                                doc.getString("phone")
                        );
                        callback.onSuccess(profile);
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void saveUser(Profile profile, @NonNull ProfileCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("deviceID", profile.getDeviceID());
        data.put("name", profile.getName());
        data.put("email", profile.getEmail());
        data.put("phone", profile.getPhone());

        usersRef.document(profile.getDeviceID())
                .set(data)
                .addOnSuccessListener(aVoid -> callback.onSuccess(profile))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteUser(String deviceID, @NonNull ProfileCallback callback) {
        usersRef.document(deviceID)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onDeleted())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        return new ArrayList<>();
    }

    // ===== DeviceID-based Auth operations =====

    @Override
    public void userExists(String email, UserExistsCallback callback) {
        usersRef.whereEqualTo("email", email).get()
                .addOnSuccessListener(querySnapshot ->
                        callback.onResult(!querySnapshot.isEmpty()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    @Override
    public void login(String email, String deviceID, LoginCallback callback) {
        usersRef.whereEqualTo("email", email).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onResult(false, "Email not registered");
                        return;
                    }

                    String storedDeviceID = querySnapshot.getDocuments().get(0).getString("deviceID");
                    if (deviceID.equals(storedDeviceID)) {
                        callback.onResult(true, "Login successful");
                    } else {
                        callback.onResult(false, "Device ID does not match");
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false, e.getMessage()));
    }

    @Override
    public void register(String email, String phone, String name, String deviceID, RegisterCallback callback) {
        userExists(email, exists -> {
            if (exists) {
                callback.onResult(false, "User already exists");
                return;
            }

            Profile profile = new Profile(deviceID, name, email, phone);
            Map<String, Object> data = new HashMap<>();
            data.put("deviceID", deviceID);
            data.put("name", name);
            data.put("email", email);
            data.put("phone", phone);

            usersRef.document(deviceID)
                    .set(data)
                    .addOnSuccessListener(aVoid -> callback.onResult(true, "Registration successful"))
                    .addOnFailureListener(e -> callback.onResult(false, e.getMessage()));
        });
    }
}