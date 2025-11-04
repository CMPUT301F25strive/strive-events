package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class EventRepositoryTest {

    private Event mockEvent() {
        return new Event(1, "Valorant", "Tournament", "Home", 200, 15.0f, 1, 1, true, new WaitingList(null));
    }

    private EventRepository mockEventRepo() {
        EventRepository eventRepo = new EventRepository();
        eventRepo.add(mockEvent());
        return eventRepo;
    }

    @Test
    public void testAddandCountEvents() {
        EventRepository eventRepo = new EventRepository();
        assertEquals(0, eventRepo.countEvents());

        Event event1 = mockEvent();
        eventRepo.add(event1);
        assertEquals(1, eventRepo.countEvents());

        Event event2 = new Event(2, "LOL", "Worlds", "Stage", 250, 20.0f, 2, 2, false, new WaitingList(null));
        eventRepo.add(event2);
        assertEquals(2, eventRepo.countEvents());
    }

    @Test
    public void testGetEvent() {
        EventRepository eventRepo = mockEventRepo();
        assertEquals(1, eventRepo.countEvents());
        Event event1 = eventRepo.getEvent(1);
        assertEquals("Valorant", event1.getName());
        assertEquals("Home", event1.getVenue());
        assertEquals(200, event1.getCapacity());
    }

    @Test
    public void testGetEventException() {
        EventRepository eventRepo = mockEventRepo();
        assertThrows(IllegalArgumentException.class, () -> {eventRepo.getEvent(999);});
    }

    @Test
    public void testDeleteEvent() {
        EventRepository eventRepo = mockEventRepo();
        assertEquals(1, eventRepo.countEvents());
        eventRepo.deleteEvent(1);
        assertEquals(0, eventRepo.countEvents());
    }

}

