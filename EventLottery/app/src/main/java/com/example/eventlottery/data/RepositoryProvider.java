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

    @NonNull
    public static synchronized EventRepository getEventRepository() {
        if (eventRepository == null) {
            //eventRepository = new FakeEventRepository();
            eventRepository = new FirebaseEventRepository();
        }
        return eventRepository;
    }

    @NonNull
    public static synchronized ProfileRepository getProfileRepository() {
        if (profileRepository == null) {
            profileRepository = new ProfileRepository();
        }
        return profileRepository;
    }
}
