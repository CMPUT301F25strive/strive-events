package com.example.eventlottery.data;

import androidx.annotation.NonNull;

import com.example.eventlottery.model.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseProfileRepository implements ProfileRepository {

    private final FirebaseFirestore db;
    private final CollectionReference usersRef;
    private final FirebaseAuth auth;

    public FirebaseProfileRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void findUserById(String id, @NonNull ProfileCallback callback) {
        usersRef.document(id).get()
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
    public void deleteUser(String id, @NonNull ProfileCallback callback) {
        usersRef.document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null && currentUser.getUid().equals(id)) {
                        currentUser.delete()
                                .addOnSuccessListener(unused -> callback.onDeleted())
                                .addOnFailureListener(e ->
                                        callback.onError("Firestore deleted but Auth user not deleted: " + e.getMessage()));
                    } else {
                        callback.onDeleted();
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public List<Profile> findUsersByRole(Profile.Role role) {
        return new ArrayList<>();
    }

    // ===== Firebase Authentication Integration =====

    @Override
    public void userExists(String email, UserExistsCallback callback) {
        auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result ->
                        callback.onResult(result.getSignInMethods() != null &&
                                !result.getSignInMethods().isEmpty()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    @Override
    public void login(String email, String password, LoginCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        callback.onResult(true, "Login successful");
                    } else {
                        callback.onResult(false, "User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false, e.getMessage()));
    }

    @Override
    public void register(String email, String password, String phone, String name, String deviceID, RegisterCallback callback) {
        userExists(email, exists -> {
            if (exists) {
                callback.onResult(false, "User already exists");
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user == null) {
                                callback.onResult(false, "Registration failed: no user created");
                                return;
                            }

                            String uid = user.getUid();
                            Profile profile = new Profile(deviceID, name, email, phone);

                            Map<String, Object> data = new HashMap<>();
                            data.put("deviceID", deviceID);
                            data.put("name", name);
                            data.put("email", email);
                            data.put("phone", phone);

                            usersRef.document(uid)
                                    .set(data)
                                    .addOnSuccessListener(aVoid ->
                                            callback.onResult(true, "Registration successful"))
                                    .addOnFailureListener(e ->
                                            callback.onResult(false, e.getMessage()));
                        })
                        .addOnFailureListener(e -> callback.onResult(false, e.getMessage()));
            }
        });
    }
}