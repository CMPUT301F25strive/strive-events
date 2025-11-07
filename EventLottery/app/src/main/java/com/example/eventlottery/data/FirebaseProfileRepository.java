package com.example.eventlottery.data;

import android.util.Log;

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

                        if (users != null) {
                            users.add(new Profile(userID, name, email, phone, role));
                        }
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

    @Override
    public Profile findUserById(String deviceId) {
        Log.d("ProfileRepository", "Searching for deviceId: " + deviceId);
        for (Profile user : users) {
            Log.d("ProfileRepository", "Checking user: " + user.getDeviceID());
            if (deviceId.equals(user.getDeviceID())) {
                Log.d("ProfileRepository", "Found user: " + user.getName());
                return user;
            }
        }
        Log.d("ProfileRepository", "User not found");
        return null;
    }

    @Override
    public void saveUser(Profile profile) {
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (profile.getDeviceID().equals(users.get(i).getDeviceID())) {
                users.set(i, profile);
                found = true;
                break;
            }
        }
        if (!found) {
            users.add(profile);
        }

        // Save to Firebase
        Map<String, Object> data = new HashMap<>();
        data.put("name", profile.getName());
        data.put("email", profile.getEmail());
        data.put("phone", profile.getPhone());
        data.put("role", profile.getRole().name());

        usersRef.document(profile.getDeviceID())
            .set(data)
            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Profile saved"))
            .addOnFailureListener(e -> Log.e("Firestore", "Save failed", e));
    }

    @Override
    public void deleteUser(String id) {
        users.removeIf(profile -> id.equals(profile.getDeviceID()));
        
        usersRef.document(id).delete()
            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Profile deleted"))
            .addOnFailureListener(e -> Log.e("Firestore", "Delete failed", e));
    }

    // For admin controller
    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        List<Profile> result = new ArrayList<>();
        for (Profile profile : users) {
            if (profile.getRole() == role) {
                result.add(profile);
            }
        }
        return result;
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
}
