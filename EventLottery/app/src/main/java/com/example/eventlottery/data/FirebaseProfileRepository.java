package com.example.eventlottery.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventlottery.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds CRUD for entrant and organizer profiles.
 * This provides search/browse for admin.
 */
public class FirebaseProfileRepository implements ProfileRepository {
    private List<Profile> users = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    /**
     * This is full constructor of FirebaseProfileRepository that has all necessary initialization for the firebase implementation
     */
    public FirebaseProfileRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");  // Single collection for all roles

        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    users.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String userID = doc.getId();
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");

                        if (users != null) { users.add(new Profile(userID, name, email, phone)); }
                    }
                }
            }
        });

        for (Profile user : users) {
            Log.d("Users", "Loaded user: " + user.getName());
        }
        usersRef.get().addOnSuccessListener(qs -> {
            Log.d("FirestoreProfiles", "Direct get(): " + qs.size() + " documents");
            for (var doc : qs) {
                Log.d("FirestoreProfiles", "Doc: " + doc.getId() + " => " + doc.getData());
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreProfiles", "Direct get() failed", e);
        });
    }

    // ===== Firestore operations =====

    /**
     * This method finds the user's profile based on their unique device id
     * @param deviceID: unique device id specific to user
     * @return u: user that matches the inputted device id
     */
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

    /**
     * This method finds the user's profile based on their unique device id
     * @param deviceID: unique device id specific to user
     * @return user: user that matches the inputted device id
     */
    public Profile findUserById(String deviceID) {
        for (Profile user : users) {
            if (user.getDeviceID().equals(deviceID)) {
                return user;
            }
        }
        return null;
    }

    /**
     * This methods saves the user's profile into the firebase repository for profiles
     * @param profile: profile that is desired to be saved in the firebase repository
     */
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
    public void saveUser(Profile profile) {

    }

    @Override
    public void deleteUser(String id) {

    }

    /**
     * This methods filters the user's profile based off their roles
     * @param role: the role of the user
     * @return result: list of all the profiles that have the desired roll
     */
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

    private Profile.Role parseRole(String roleString) {
        if (roleString == null) {
            return Profile.Role.USER;
        }
        try {
            return Profile.Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Profile.Role.USER;
        }
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