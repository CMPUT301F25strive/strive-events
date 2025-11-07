package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.example.eventlottery.data.MockEventRepository;
import com.example.eventlottery.model.Event;

import org.junit.Test;

/**
 * This is a test class for the event repository
 */
public class EventRepositoryTest {

    private Event mockEvent() {
        return new Event("1", "Valorant", "Tenz", 0,"Home", 200, 200, Event.Status.REG_OPEN, 1, "Tournament");
    }

    private MockEventRepository mockEventRepo() {
        MockEventRepository eventRepo = new MockEventRepository();
        eventRepo.add(mockEvent());
        return eventRepo;
    }

    @Test
    public void testAddandCountEvents() {
        MockEventRepository eventRepo = new MockEventRepository();
        assertEquals(0, eventRepo.getSize());

        Event event1 = mockEvent();
        eventRepo.add(event1);
        assertEquals(1, eventRepo.getSize());

        Event event2 = new Event("2", "LOL", "T1", 0, "Stage", 250, 200, Event.Status.REG_OPEN, 2, "Worlds");
        eventRepo.add(event2);
        assertEquals(2, eventRepo.getSize());
    }

    @Test
    public void testGetEvent() {
        MockEventRepository eventRepo = mockEventRepo();
        assertEquals(1, eventRepo.getSize());
        Event event1 = eventRepo.getEvent("1");
        assertEquals("Valorant", event1.getTitle());
        assertEquals("Home", event1.getVenue());
        assertEquals(200, event1.getCapacity());
    }

    @Test
    public void testGetEventException() {
        MockEventRepository eventRepo = mockEventRepo();
        assertThrows(IllegalArgumentException.class, () -> {eventRepo.getEvent("999");});
    }

    @Test
    public void testDeleteEvent() {
        MockEventRepository eventRepo = mockEventRepo();
        assertEquals(1, eventRepo.getSize());
        eventRepo.deleteEvent("1");
        assertEquals(0, eventRepo.getSize());
    }

}

