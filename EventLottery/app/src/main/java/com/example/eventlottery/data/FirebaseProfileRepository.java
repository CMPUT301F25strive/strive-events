package com.example.eventlottery.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
    private final List<Profile> users = new ArrayList<>();
    private final MutableLiveData<List<Profile>> profilesLiveData = new MutableLiveData<>(new ArrayList<>());
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
                        String roleString = doc.getString("role");
                        Profile.Role role = parseRole(roleString);

                        users.add(new Profile(userID, name, email, phone, role));
                    }
                    profilesLiveData.postValue(new ArrayList<>(users));
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
                                doc.getString("phone"),
                                parseRole(doc.getString("role"))
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
        usersRef.document(profile.getDeviceID())
                .set(buildProfileData(profile))
                .addOnSuccessListener(aVoid -> callback.onSuccess(profile))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * This methods deletes a user's profile from the firebase repository for profiles
     * @param deviceID: the device ID of the user whose profile will be deleted
     * @param callback : ensures deletion of user's profile
     */
    @Override
    public void deleteUser(String deviceID, @NonNull ProfileCallback callback) {
        usersRef.document(deviceID)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onDeleted())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * This methods saves the user's profile into the firebase repository for profiles
     * @param profile: profile that is desired to be saved in the firebase repository
     */
    @Override
    public void saveUser(Profile profile) {
        usersRef.document(profile.getDeviceID())
                .set(buildProfileData(profile));
    }

    /**
     * This methods deletes a user's profile from the firebase repository for profiles
     * @param id: the device ID of the user whose profile will be deleted
     */
    @Override
    public void deleteUser(String id) {
        usersRef.document(id).delete();
    }

    /**
     * This methods filters the user's profile based off their roles
     * @param role: the role of the user
     * @return result: list of all the profiles that have the desired roll
     */
    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        List<Profile> result = new ArrayList<>();
        for (Profile user : users) {
            if (user.getRole() == role) {
                result.add(user);
            }
        }
        return result;
    }

    /**
     * @return profilesLiveData
     */
    @Override
    public LiveData<List<Profile>> observeProfiles() {
        return profilesLiveData;
    }

    // ===== DeviceID-based Auth operations =====

    /**
     * This methods checks if a user exists
     * @param email: the email of the user
     */
    @Override
    public void userExists(String email, UserExistsCallback callback) {
        usersRef.whereEqualTo("email", email).get()
                .addOnSuccessListener(querySnapshot ->
                        callback.onResult(!querySnapshot.isEmpty()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    /**
     * This methods logs a user in
     * @param email: the email of the user
     * @param deviceID: the device ID of the user
     */
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

    /**
     * This methods checks and assigns a role to the user
     * @param roleString: the role of the user
     */
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

    /**
     * This methods registers a user
     * @param email : the email of the user
     * @param phone : the phone of the user
     * @param deviceID : device ID of the user
     */
    @Override
    public void register(String email, String phone, String name, String deviceID, RegisterCallback callback) {
        userExists(email, exists -> {
            if (exists) {
                callback.onResult(false, "User already exists");
                return;
            }

            Profile profile = new Profile(deviceID, name, email, phone);
            Map<String, Object> data = buildProfileData(profile);

            usersRef.document(deviceID)
                    .set(data)
                    .addOnSuccessListener(aVoid -> callback.onResult(true, "Registration successful"))
                    .addOnFailureListener(e -> callback.onResult(false, e.getMessage()));
        });
    }

    /**
     * This methods build a hash for a user profile
     * @param profile : the profile of the user
     * @return : the hash of the profile
     */
    private Map<String, Object> buildProfileData(Profile profile) {
        Map<String, Object> data = new HashMap<>();
        data.put("deviceID", profile.getDeviceID());
        data.put("name", profile.getName());
        data.put("email", profile.getEmail());
        data.put("phone", profile.getPhone());
        data.put("role", profile.getRole().name());
        return data;
    }
}
