package com.example.eventlottery.viewmodel;

import android.companion.DeviceId;
import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.credentials.exceptions.domerrors.DataError;
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
 * This is the view model for MyEventsFragment.
 */

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

    /**
     * Constructor for MyEventsViewModel.
     * @param context
     */
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

    /**
     * Getter for state.
     * @return LiveData<EventListUiState>
     */
    @NonNull
    public LiveData<EventListUiState> getState() {
        return state;
    }

    /**
     * Getter for current segment.
     * @return LiveData<EventSegment>
     */
    public LiveData<EventSegment> getCurrentSegment() {
        return currentSegment;
    }

    /**
     * Switch for current segment.
     * @param segment
     */
    public void switchSegment(EventSegment segment) {
        currentSegment.setValue(segment);
    }

    /**
     * Filter events with different segments based on state of entrants, time, and status of events.
     *  Suppose (attendeesList ∪ canceledList) ⊆ invitedList ⊆ waitingList
     * @param events: the list of event objects
     * @param segment: Different views of MyEvent page
     * @return the filtered events
     */
    private List<Event> filterEventsBySegment(List<Event> events, EventSegment segment) {
        List<Event> filteredEvents = new ArrayList<>();
        if (events == null) return filteredEvents;

        for (Event event : events) {
            // Initialize conditions to segment events
            boolean isOnWaitingList = event.isOnWaitingList(deviceId);
            //boolean isInvited = event.isOnInvitedList(deviceId);
            boolean isAttending = event.isOnAttendeesList(deviceId);
            boolean isCanceled = event.isOnCanceledList(deviceId);
            boolean isOrganizer = deviceId.equals(event.getOrganizerId());
            boolean isEventStarted = event.isEventStarted();

            switch (segment) {
                case WAITING_LIST:
                    // Entrant view: still on waiting list
                    // or invited but not yet accepted/declined
                    // and the event in the future and not finalized
                    if (!isEventStarted && !event.isFinalized()
                            && !isAttending
                            && !isCanceled
                            && isOnWaitingList) {
                        filteredEvents.add(event);
                    }
                    break;

                case ACCEPTED:
                    // Entrant view: have confirmed to attend an event that hasn't started yet
                    if (isAttending && !isEventStarted && !isCanceled) {
                        filteredEvents.add(event);
                    }
                    break;

                case HISTORY:
                    // Entrant view: after the event has started, or the user declined it
                    if ( (isOnWaitingList && (isEventStarted || event.isFinalized()) )
                            || isCanceled) {
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

    /**
     * Refresh the list of events.
     */
    public void refresh() {
        repository.refresh();
    }
}