package com.example.eventlottery.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventlottery.model.Event;
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
import java.util.Objects;

public class FirebaseEventRepository implements EventRepository {

    private List<Event> events = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();

    public FirebaseEventRepository() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    events.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String description = doc.getString("description");
                        String venue = doc.getString("venue");

                        Long capacityLong = doc.getLong("capacity");
                        int capacity = capacityLong != null ? capacityLong.intValue() : 0;

                        String organizerName = doc.getString("organizerName");
                        if (organizerName == null) organizerName = "Organizer";

                        Long startTimeMillisLong = doc.getLong("startTimeMillis");
                        long startTimeMillis = startTimeMillisLong != null ? startTimeMillisLong : System.currentTimeMillis();

                        Long spotsRemainingLong = doc.getLong("spotsRemaining");
                        int spotsRemaining = spotsRemainingLong != null ? spotsRemainingLong.intValue() : 0;

                        String statusStr = doc.getString("status");
                        Event.Status status = statusStr != null ? Event.Status.valueOf(statusStr) : Event.Status.REG_OPEN;

                        Long posterResIdLong = doc.getLong("posterResId");
                        int posterResId = posterResIdLong != null ? posterResIdLong.intValue() : 0;

                        List<String> waitingList = (List<String>) doc.get("waitingList");

                        events.add(new Event(
                                id, name, organizerName, startTimeMillis,
                                venue, capacity, spotsRemaining, status,
                                posterResId, description, waitingList
                        ));
                    }
                    eventsLiveData.setValue(new ArrayList<>(events));
                }
            }
        });
    }

    @Override
    public Event findEventById(String eventID) {
        for (Event event : events) {
            if (Objects.equals(eventID, event.getId())) {
                return event;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public LiveData<List<Event>> observeEvents() {
        return eventsLiveData;
    }

    @Override
    public void refresh() {}

    @Override
    public void updateWaitingList(@NonNull String eventId, @NonNull List<String> waitingList) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("waitingList", waitingList);

        eventsRef.document(eventId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Waiting list updated for event: " + eventId))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating waiting list", e));
    }

    @Override
    public void deleteEvent(@NonNull String eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (Objects.equals(eventID, events.get(i).getId())) {
                events.remove(i);
                eventsRef.document(eventID).delete();
                eventsLiveData.setValue(new ArrayList<>(events));
                break;
            }
        }
    }
}