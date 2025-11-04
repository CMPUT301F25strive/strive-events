package com.example.eventlottery.data;

import androidx.annotation.NonNull;

/**
 * simple locator so we can swap repository implementations later.
 */
public final class RepositoryProvider {

    private static EventRepository eventRepository;

    private RepositoryProvider() {
    }

    @NonNull
    public static synchronized EventRepository getEventRepository() {
        if (eventRepository == null) {
            eventRepository = new FakeEventRepository();
        }
        return eventRepository;
    }
}
