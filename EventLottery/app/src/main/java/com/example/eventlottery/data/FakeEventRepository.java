package com.example.eventlottery.data;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventlottery.R;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Event.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * in-memory list used until our firestore integration is ready
 */
public class FakeEventRepository implements EventRepository {

    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();

    public FakeEventRepository() {
        loadSeedData();
    }

    @NonNull
    @Override
    public LiveData<List<Event>> observeEvents() {
        return eventsLiveData;
    }

    @Override
    public void refresh() {
        // keep static data for now so ui stays predictable
        loadSeedData();
    }

    @MainThread
    private void loadSeedData() {
        List<Event> seed = new ArrayList<>();
        seed.add(new Event(
                "evt-001",
                "Community Swim Challenge",
                "Jane",
                toMillis(2025, 1, 19, 19, 30),
                "Community centre",
                50,
                12,
                Status.REG_OPEN,
                R.drawable.poster_swim,
                "Shake off the winter chill with a friendly swim meet for all skill levels."
        ));
        seed.add(new Event(
                "evt-002",
                "Puppy Training for beginners",
                "Jane",
                toMillis(2025, 1, 19, 19, 30),
                "Community centre",
                25,
                4,
                Status.REG_OPEN,
                R.drawable.poster_puppy,
                "Learn positive reinforcement basics with local trainers and friendly pups."
        ));
        seed.add(new Event(
                "evt-003",
                "Piano Lessons Intermediate",
                "Jane",
                toMillis(2025, 1, 19, 19, 30),
                "Rutherford Library",
                18,
                0,
                Status.REG_CLOSED,
                R.drawable.poster_piano,
                "Hands-on coaching session for returning students polishing their recital set."
        ));
        seed.add(new Event(
                "evt-004",
                "Mindful Morning Yoga",
                "Aisha",
                toMillis(2025, 1, 20, 9, 0),
                "Westside studio",
                32,
                9,
                Status.REG_OPEN,
                R.drawable.poster_wellness,
                "Start the week with a gentle flow focused on balance, breath, and reset."
        ));
        List<Event> openEvents = new ArrayList<>();
        for (Event event : seed) {
            if (event.getStatus() == Status.REG_OPEN) {
                openEvents.add(event);
            }
        }
        eventsLiveData.setValue(Collections.unmodifiableList(openEvents));
    }

    private long toMillis(int year, int month, int day, int hour, int minute) {
        java.util.Calendar calendar = java.util.GregorianCalendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(year, month - 1, day, hour, minute, 0);
        return calendar.getTimeInMillis();
    }
}
