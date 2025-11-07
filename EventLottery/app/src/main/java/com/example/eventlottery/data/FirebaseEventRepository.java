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
import java.util.Objects;

import com.example.eventlottery.model.Event;

/**
 * This class holds our Event objects
 */
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
                    for (QueryDocumentSnapshot doc: querySnapshots) {
                        String id = doc.getId();    // doc ID should be string
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
                        int posterResId = posterResIdLong != null ? posterResIdLong.intValue() : 0; //

                        List<String> waitingList = (List<String>) doc.get("waitingList");
                        if (waitingList == null) waitingList = new ArrayList<>();

                        Event event = new Event(
                                id,
                                name,
                                organizerName,
                                startTimeMillis,
                                venue,
                                capacity,
                                spotsRemaining,
                                status,
                                posterResId,
                                description
                        );
                        event.setWaitingList(waitingList);
                        events.add(event);
                        Log.d("Firestore", String.format("Event(%s, %s) fetched", id, name));

                    }
                    eventsLiveData.setValue(new ArrayList<>(events));
                }
            }
        });
    }

    @Override
    public Event findEventById(String eventID) {
        Log.d("EventRepository", "Searching for eventID: " + eventID);
        for (Event event : events) {
            Log.d("EventRepository", "Checking events: " + event.getId());
            if (eventID.equals(event.getId())) {
                Log.d("EventRepository", "Found event: " + event.getTitle());
                return event;
            }
        }
        Log.d("EventRepository", "Event not found");
        return null;
    }

    @NonNull
    @Override
    public LiveData<List<Event>> observeEvents() {
        return eventsLiveData;
    }

    @Override
    public void refresh() {}    //

    /**
     * This method adds an Event type object to the events list
     * @param e: the object to add
     */
    public void add(Event e) {
        events.add(e);
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        HashMap<String, Object> data = new HashMap<>();
        data.put("eventID", e.getId());
        data.put("name", e.getTitle());
        data.put("description", e.getDescription());
        data.put("venue", e.getVenue());
        data.put("capacity", e.getCapacity());
        data.put("organizerName", e.getOrganizerName());
        data.put("startTimeMillis", e.getStartTimeMillis());
        data.put("spotsRemaining", e.getSpotsRemaining());
        data.put("status", e.getStatus().name());
        data.put("posterResId", e.getPosterResId());
        data.put("waitingList", e.getWaitingList());
        data.put("waitingListSize", e.getWaitingListSize());
        //data.put("organizerID", e.getOrganizerID());
        //data.put("posterID", e.getPosterID());
        //data.put("geoRequired", e.getGeoRequired());
        eventsRef.document(e.getId()).set(data);
    }

    /**
     * This method gets the event list
     * @return list: the list of events to be returned
     */
    public List<Event> getEvents() {
        List<Event> list = events;
        return list;
    }

    // We can overload before confirming the type of id
    /**
     * This method gets the event list from an EventID
     * @return Event: the event with the corresponding eventID
     * @throws IllegalArgumentException: if no event is associated with that ID exists
     */
    public Event getEvent(int eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (String.valueOf(eventID).equals(events.get(i).getId())) {
                return events.get(i);
            }
        }
        throw new IllegalArgumentException();

    }
    public Event getEvent(String eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (Objects.equals(eventID, events.get(i).getId())) {
                return events.get(i);
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * This method removes an Event from the list using the event's ID if it exists in the list
     */
    public void deleteEvent(int eventID) {
        for (int i = 0; i < events.size(); i++) {
            String strID = String.valueOf(eventID);
            if (strID.equals(events.get(i).getId())) {
                events.remove(events.get(i));

                eventsRef.document(strID).delete();  // Delete from the Firebase
                eventsLiveData.setValue(new ArrayList<>(events));   // Update UI
                break;
            }
        }
    }
    // Overload
    public void deleteEvent(String eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (Objects.equals(eventID, events.get(i).getId())) {
                events.remove(events.get(i));
                eventsRef.document(eventID).delete();
                eventsLiveData.setValue(new ArrayList<>(events));
                break;
            }
        }
    }

    /**
     * This counts the amount of events in the list
     * @return int: the number of events in the list
     */
    public int getSize() {
        return events.size();
    }

    @Override
    public void updateWaitingList(String eventId, List<String> waitingList) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("waitingList", waitingList);

        eventsRef.document(eventId)
                .update(updateData)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Waiting list updated for event: " + eventId))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error updating waiting list", e));
    }
}
