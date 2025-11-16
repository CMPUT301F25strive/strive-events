package com.example.eventlottery.viewmodel;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private final String deviceId;
    private final MutableLiveData<EventSegment> currentSegment = new MutableLiveData<>(EventSegment.WAITING_LIST);
    private final LiveData<EventListUiState> state;

    public enum EventSegment {
        WAITING_LIST,
        ACCEPTED,
        HISTORY
    }

    public MyEventsViewModel(@NonNull Context context) {
        repository = RepositoryProvider.getEventRepository();
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Transform both repository events and segment changes into UI state
        state = Transformations.switchMap(currentSegment, segment ->
                Transformations.map(repository.observeEvents(), events -> {
                    List<Event> filteredEvents = filterEventsBySegment(events, segment);
                    return new EventListUiState(false, filteredEvents, null);
                })
        );
    }

    @NonNull
    public LiveData<EventListUiState> getState() {
        return state;
    }

    public LiveData<EventSegment> getCurrentSegment() {
        return currentSegment;
    }

    public void switchSegment(EventSegment segment) {
        currentSegment.setValue(segment);
    }

    private List<Event> filterEventsBySegment(List<Event> events, EventSegment segment) {
        List<Event> filteredEvents = new ArrayList<>();
        if (events == null) return filteredEvents;

        long currentTime = System.currentTimeMillis();

        for (Event event : events) {
            boolean isOnWaitingList = event.isOnWaitingList(deviceId);
            boolean isAttending = event.getAttendeesListSize() > 0 && event.getAttendeesList().contains(deviceId);

            switch (segment) {
                case WAITING_LIST:
                    if (isOnWaitingList && !isAttending) {
                        filteredEvents.add(event);
                    }
                    break;

                case ACCEPTED:
                    if (isAttending) {
                        filteredEvents.add(event);
                    }
                    break;

                case HISTORY:
                    if (isAttending || isOnWaitingList) {
                        filteredEvents.add(event);
                    }
                    break;
            }
        }
        return filteredEvents;
    }

    /**
     * Refresh all events from repository (pull-to-refresh or manual)
     */
    public void refresh() {
        repository.refresh();
    }
}