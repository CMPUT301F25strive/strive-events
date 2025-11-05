package com.example.eventlottery;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class holds CRUD for entrant and organizer profiles.
 * This provides search/browse for admin.
 */
public class ProfileRepository {
    private List<UserProfile> users = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    public ProfileRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    users.clear();
                    for (QueryDocumentSnapshot doc: querySnapshots) {
                        String userID = doc.getId(); // document ID as int
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");

                        users.add(new UserProfile(userID, name, email, phone));
                    }
                }
            }
        });
    }


    /**
     * This method adds an user type object to the users list
     * @param user: the object to add
     */
    public void add(UserProfile user) {
        users.add(user);
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        HashMap<String, Object> data = new HashMap<>();
        data.put("deviceID", user.getDeviceID());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        usersRef.document(String.valueOf(user.getDeviceID())).set(data);
    }

    /**
     * This method gets the users list
     * @return list: the list of users to be returned
     */
    public List<UserProfile> getUsers() {
        List<UserProfile> list = users;
        return list;
    }
    /**
     * This method gets the user from a DeviceID
     * @return Event: the user with the corresponding DeviceID
     * @throws IllegalArgumentException: if no user is associated with that ID exists
     */
    public UserProfile getUser(String deviceID) {
        for (int i = 0; i < users.size(); i++) {
            if (deviceID.equals(users.get(i).getDeviceID())) {
                return users.get(i);
            }
        }
        throw new IllegalArgumentException();

    }



    /**
     * This saves an entrant profile.
     * @param entrant: entrant profile
     */
    //void saveEntrant(EntrantProfile entrant);

    /**
     * This deletes an entrant profile by its unique id.
     * @param userID: the value of the user's unique key
     */
    public void deleteUser(String userID) {
        for (int i = 0; i < users.size(); i++) {
            if (userID.equals(users.get(i).getDeviceID())) {
                users.remove(users.get(i));
            }
        }
    }

    // TODO: For admin browse and search
    /**
     * This searches the desired entrant profiles.
     * @param criteria:
     * @return a list of entrant profiles
     */
    //List<EntrantProfile> searchEntrants(String criteria);

    /**
     * This searches the desired organizer profiles.
     * @param cirteria:
     * @return a list of organizer profiles
     */
    //List<OrganizerProfile> searchOrganizers(String criteria);

    /**
     * This counts the amount of events in the list
     * @return int: the number of events in the list
     */
    public int getSize() {
        return users.size();
    }
}


