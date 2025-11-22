package com.example.eventlottery;

import static com.example.eventlottery.model.Event.Status.REG_OPEN;
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
        return new Event("1", "Valorant Tournament", "Tenz",
                1024, 526, 886, "Los Angeles", 2, 2, REG_OPEN, "", "A Valorant Tournament hosted by Tenz for a prize of $1000", Event.Tag.PARTY);
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

        Event event2 = new Event("2", "LOL", "T1", 0, 10, 20, "Stage", 250, 200, Event.Status.REG_OPEN, "", "Worlds", Event.Tag.PARTY);
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
        assertEquals("Valorant Tournament", event1.getTitle());
        assertEquals("Los Angeles", event1.getVenue());
        assertEquals(2, event1.getCapacity());
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

