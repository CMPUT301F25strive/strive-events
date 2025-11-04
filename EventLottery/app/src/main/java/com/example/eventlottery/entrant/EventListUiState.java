package com.example.eventlottery.entrant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventlottery.model.Event;

import java.util.Collections;
import java.util.List;

/**
 * value object backing the entrant event feed.
 */
public class EventListUiState {

    public final boolean loading;
    @NonNull
    public final List<Event> events;
    @Nullable
    public final String errorMessage;

    public EventListUiState(boolean loading, @NonNull List<Event> events, @Nullable String errorMessage) {
        this.loading = loading;
        this.events = Collections.unmodifiableList(events);
        this.errorMessage = errorMessage;
    }

    @NonNull
    public static EventListUiState loading() {
        return new EventListUiState(true, Collections.emptyList(), null);
    }
}
