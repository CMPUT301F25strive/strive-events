package com.example.eventlottery.data;

import static org.junit.Assert.assertEquals;

import com.example.eventlottery.TestLiveDataUtil;
import com.example.eventlottery.model.Event;

import org.junit.Test;

import java.util.List;

public class FakeEventRepositoryTest {

    @Test
    public void observeEvents_returnsSeedList() throws InterruptedException {
        FakeEventRepository repository = new FakeEventRepository();
        List<Event> events = TestLiveDataUtil.getOrAwaitValue(repository.observeEvents());
        assertEquals(3, events.size());
        assertEquals("Community Swim Challenge", events.get(0).getTitle());
        for (Event event : events) {
            assertEquals(Event.Status.REG_OPEN, event.getStatus());
        }
    }

    @Test
    public void refresh_reEmitsSeedList() throws InterruptedException {
        FakeEventRepository repository = new FakeEventRepository();
        // drain initial emission
        TestLiveDataUtil.getOrAwaitValue(repository.observeEvents());
        repository.refresh();
        List<Event> events = TestLiveDataUtil.getOrAwaitValue(repository.observeEvents());
        assertEquals(3, events.size());
        assertEquals("Mindful Morning Yoga", events.get(2).getTitle());
        for (Event event : events) {
            assertEquals(Event.Status.REG_OPEN, event.getStatus());
        }
    }
}
