package com.example.eventlottery.data;

import androidx.annotation.NonNull;

/**
 * Simple locator so we can swap repository implementations later.
 */
public final class RepositoryProvider {

    private static EventRepository eventRepository;
    private static ProfileRepository profileRepository;

    private RepositoryProvider() {
        // Prevent instantiation
    }

    /**
     * @return singleton instance of EventRepository
     */
    @NonNull
    public static synchronized EventRepository getEventRepository() {
        if (eventRepository == null) {
            // You can switch implementations here
            // eventRepository = new FakeEventRepository();
            eventRepository = new FirebaseEventRepository();
        }
        return eventRepository;
    }

    /**
     * @return singleton instance of ProfileRepository
     */
    @NonNull
    public static synchronized ProfileRepository getProfileRepository() {
        if (profileRepository == null) {
            profileRepository = new FirebaseProfileRepository();
        }
        return profileRepository;
    }
}