package com.example.eventlottery.data;

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
import com.example.eventlottery.model.Event;

/**
 * This class holds our Event objects
 */
public class EventRepository {
    private List<Event> events = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    public EventRepository() {
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
                        int eventID = Integer.parseInt(doc.getId()); // document ID as int
                        String name = doc.getString("name");
                        String description = doc.getString("description");
                        String venue = doc.getString("venue");

                        Long capacityLong = doc.getLong("capacity");
                        int capacity = capacityLong != null ? capacityLong.intValue() : 0;

                        Double priceDouble = doc.getDouble("price");
                        float price = priceDouble != null ? priceDouble.floatValue() : 0;

                        Long organizerLong = doc.getLong("organizerID");
                        int organizerID = organizerLong != null ? organizerLong.intValue() : 0;

                        Long posterLong = doc.getLong("posterID");
                        int posterID = posterLong != null ? posterLong.intValue() : 0;

                        Boolean geoRequired = doc.getBoolean("geoRequired");
                        if (geoRequired == null) geoRequired = false;

                        Log.d("Firestore", String.format("Event(%d, %s) fetched", eventID, name));
                        events.add(new Event(eventID, name, description, venue, capacity, price, organizerID, posterID, geoRequired));
                    }
                }
            }
        });
    }
    /**
     * This method adds an Event type object to the events list
     * @param e: the object to add
     */
    public void add(Event e) {
        events.add(e);
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        HashMap<String, Object> data = new HashMap<>();
        data.put("eventID", e.getEventID());
        data.put("name", e.getName());
        data.put("description", e.getDescription());
        data.put("venue", e.getVenue());
        data.put("capacity", e.getCapacity());
        data.put("price", e.getPrice());
        data.put("organizerID", e.getOrganizerID());
        data.put("posterID", e.getPosterID());
        data.put("geoRequired", e.getGeoRequired());
        eventsRef.document(String.valueOf(e.getEventID())).set(data);
    }

    /**
     * This method gets the event list
     * @return list: the list of events to be returned
     */
    public List<Event> getEvents() {
        List<Event> list = events;
        return list;
    }

    /**
     * This method gets the event list from an EventID
     * @return Event: the event with the corresponding eventID
     * @throws IllegalArgumentException: if no event is associated with that ID exists
     */
    public Event getEvent(int eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (eventID == events.get(i).getEventID()) {
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
            if (eventID == events.get(i).getEventID()) {
                events.remove(events.get(i));
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
}
