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
    /**
     * This will create a mockEvent for test
     * @return the mock event
     */
    private Event mockEvent() {
        return new Event("1", "Valorant", "Tenz", 0,"Home", 200, 200, Event.Status.REG_OPEN, 1, "Tournament");
    }

    /**
     * THis will create a mock event repo that includes the mock event for testing purpose.
     * @return A mock event repo
     */
    private MockEventRepository mockEventRepo() {
        MockEventRepository eventRepo = new MockEventRepository();
        eventRepo.add(mockEvent());
        return eventRepo;
    }

    /**
     * This tests the "add" function of Event repo.
     * Each time the add function executes, the event repo should have right event number.
     */
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

    /**
     * This tests the get event function of Event repo.
     * Each time an "Get Event" executes, it will get the correct event from the event repo.
     */
    @Test
    public void testGetEvent() {
        MockEventRepository eventRepo = mockEventRepo();
        assertEquals(1, eventRepo.getSize());
        Event event1 = eventRepo.getEvent("1");
        assertEquals("Valorant", event1.getTitle());
        assertEquals("Home", event1.getVenue());
        assertEquals(200, event1.getCapacity());
    }

    /**
     * This tests when trying to get a non-exist event in the event repo, an exception will arise.
     */
    @Test
    public void testGetEventException() {
        MockEventRepository eventRepo = mockEventRepo();
        assertThrows(IllegalArgumentException.class, () -> {eventRepo.getEvent("999");});
    }

    /**
     * This tests the delete function in event repo.
     * When a event is deleted, it will remove from the event repo.
     */
    @Test
    public void testDeleteEvent() {
        MockEventRepository eventRepo = mockEventRepo();
        assertEquals(1, eventRepo.getSize());
        eventRepo.deleteEvent("1");
        assertEquals(0, eventRepo.getSize());
    }

}

