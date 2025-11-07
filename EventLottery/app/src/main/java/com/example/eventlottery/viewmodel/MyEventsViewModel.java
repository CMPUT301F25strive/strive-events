package com.example.eventlottery.viewmodel;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.entrant.EventListUiState;
import com.example.eventlottery.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for MyEventsFragment, observes all events and filters
 * those where the current device is on the waiting list.
 */
public class MyEventsViewModel extends ViewModel {

    private final EventRepository repository;
    private final LiveData<EventListUiState> state;
    private final String deviceId;

    public MyEventsViewModel(@NonNull Context context) {
        repository = RepositoryProvider.getEventRepository();
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Transform repository events to UI state, filtering by deviceId in waiting list
        state = Transformations.map(repository.observeEvents(), events -> {
            List<Event> myEvents = new ArrayList<>();
            if (events != null) {
                for (Event event : events) {
                    if (event.isOnWaitingList(deviceId)) {
                        myEvents.add(event);
                    }
                }
            }
            return new EventListUiState(false, myEvents, null);
        });
    }

    @NonNull
    public LiveData<EventListUiState> getState() {
        return state;
    }

    /**
     * Refresh all events from repository (pull-to-refresh or manual)
     */
    public void refresh() {
        repository.refresh();
    }

    /**
     * Join an event's waiting list
     */
    public void joinEvent(@NonNull Event event) {
        if (!event.isOnWaitingList(deviceId)) {
            event.joinWaitingList(deviceId);
            repository.updateWaitingList(event.getId(), event.getWaitingList());
        }
    }

    /**
     * Leave an event's waiting list
     */
    public void unjoinEvent(@NonNull Event event) {
        if (event.isOnWaitingList(deviceId)) {
            event.leaveWaitingList(deviceId);
            repository.updateWaitingList(event.getId(), event.getWaitingList());
        }
    }
}