package com.example.eventlottery.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.eventlottery.model.Event;

import java.util.List;

/**
 * entry point for reading events displayed to entrants.
 */
public interface EventRepository {

    /**
     * @return live list of available events.
     */
    @NonNull
    LiveData<List<Event>> observeEvents();

    /**
     * trigger background refresh using the current data source.
     */
    void refresh();

    Event findEventById(String id);

    void updateWaitingList(String eventID, List<String> waitingList);


}