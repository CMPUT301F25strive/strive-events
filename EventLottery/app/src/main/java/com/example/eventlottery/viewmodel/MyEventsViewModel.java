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

public class MyEventsViewModel extends ViewModel {

    private final EventRepository repository;
    private final String deviceId;
    private final MutableLiveData<EventSegment> currentSegment =
            new MutableLiveData<>(EventSegment.WAITING_LIST);

    private final LiveData<EventListUiState> state;

    public enum EventSegment {
        WAITING_LIST,
        ACCEPTED,
        HISTORY,
        HOSTED
    }

    public MyEventsViewModel(@NonNull Context context) {
        repository = RepositoryProvider.getEventRepository();
        deviceId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

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

        for (Event event : events) {
            boolean isOnWaitingList = event.isOnWaitingList(deviceId);
            boolean isAttending = event.getAttendeesListSize() > 0 && event.getAttendeesList().contains(deviceId);
            boolean isOrganizer = deviceId.equals(event.getOrganizerId());
            boolean isEventStarted = event.isEventStarted();
            Event.Status status = event.getStatus();

            switch (segment) {
                case WAITING_LIST:
                    // Entrant view: still on waiting list, event in the future
                    if (isOnWaitingList && !isAttending && !isEventStarted
                            && (status != Event.Status.FINALIZED)) {
                        filteredEvents.add(event);
                    }
                    break;

                case ACCEPTED:
                    // Entrant view: have confirmed to attend an event that hasn't started yet
                    if (isAttending && !isEventStarted) {
                        filteredEvents.add(event);
                    }
                    break;

                case HISTORY:
                    // Entrant view: after the event has started, it always becomes history
                    if ((isAttending || isOnWaitingList) && isEventStarted) {
                        filteredEvents.add(event);
                    }
                    break;

                case HOSTED:
                    // Organizer view: only user's own hosted events here
                    if (isOrganizer) {
                        filteredEvents.add(event);
                    }
                    break;
            }
        }

        return filteredEvents;
    }

    public void refresh() {
        repository.refresh();
    }
}