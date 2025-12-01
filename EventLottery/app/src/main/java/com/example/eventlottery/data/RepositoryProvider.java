package com.example.eventlottery.data;

import androidx.annotation.NonNull;

/**
 * simple locator so we can swap repository implementations later.
 */
public final class RepositoryProvider {

    private static EventRepository eventRepository;
    private static ProfileRepository profileRepository;

    private RepositoryProvider() {
    }

    /**
     * Gets the EventRepository
     * @return eventRepository
     */
    @NonNull
    public static synchronized EventRepository getEventRepository() {
        if (eventRepository == null) {
            //eventRepository = new FakeEventRepository();
            eventRepository = new FirebaseEventRepository();
        }
        return eventRepository;
    }

    /**
     * Gets the ProfileRepository
     * @return profileRepository
     */
    @NonNull
    public static synchronized ProfileRepository getProfileRepository() {
        if (profileRepository == null) {
            profileRepository = new FirebaseProfileRepository();
        }
        return profileRepository;
    }

    /**
     * Test-only helper to inject a custom implementation.
     */
    public static synchronized void setProfileRepositoryForTesting(ProfileRepository repository) {
        profileRepository = repository;
    }

    /**
     * Resets cached repositories (useful for tests).
     */
    public static synchronized void resetForTesting() {
        eventRepository = null;
        profileRepository = null;
    }
}
