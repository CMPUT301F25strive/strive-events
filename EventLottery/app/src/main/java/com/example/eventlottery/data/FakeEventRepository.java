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
                "Communist Manifesto Meeting",
                "John Lennon",
                toMillis(2025, 1, 19, 19, 30),
                "Community centre",
                50,
                12,
                Status.REG_OPEN,
                R.drawable.poster_swim
        ));
        seed.add(new Event(
                "evt-002",
                "Shock Collar Training Session",
                "HasanAbi",
                toMillis(2025, 1, 19, 19, 30),
                "Community centre",
                25,
                4,
                Status.REG_OPEN,
                R.drawable.poster_puppy
        ));
        seed.add(new Event(
                "evt-003",
                "CS:GO LAN",
                "Jane",
                toMillis(2025, 1, 19, 19, 30),
                "Rutherford Library",
                18,
                0,
                Status.REG_CLOSED,
                R.drawable.poster_piano
        ));
        seed.add(new Event(
                "evt-004",
                "Furry Terrorist Anonymous",
                "Arnold Schwarzenegger",
                toMillis(2025, 1, 20, 9, 0),
                "CCIS 1-117",
                32,
                9,
                Status.REG_OPEN,
                R.drawable.poster_wellness
        ));
        eventsLiveData.setValue(Collections.unmodifiableList(seed));
    }

    private long toMillis(int year, int month, int day, int hour, int minute) {
        java.util.Calendar calendar = java.util.GregorianCalendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(year, month - 1, day, hour, minute, 0);
        return calendar.getTimeInMillis();
    }
}
