package com.example.eventlottery;

import static com.example.eventlottery.model.Event.Status.REG_OPEN;
import static org.junit.Assert.*;

import com.example.eventlottery.data.MockEventRepository;
import com.example.eventlottery.model.Event;

import org.junit.Test;

import java.util.*;

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


    @Test
    public void testAutoDraw_InitialDraw() {
        long now = System.currentTimeMillis();

        Event event = new Event("e1", "Test Event", "",
                now + 1000000,
                now - 10000,
                now - 1000,
                "Loc",
                3,
                10,
                REG_OPEN,
                null,
                "desc",
                Event.Tag.PARTY);

        // Setup lists
        event.setWaitingList(new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E")));
        event.setInvitedList(new ArrayList<>());
        event.setAttendeesList(new ArrayList<>());
        event.setCanceledList(new ArrayList<>());

        // Test if it's the valid time period
        assertTrue(event.isRegEnd());
        assertFalse(event.isEventStarted());

        // Run the logic
        MockEventRepository.runAutoDrawLogic(event);

        // Test if there are desired number of winners
        assertEquals(3, event.getInvitedList().size());
        for (String id : event.getInvitedList()) {
            // Check they are literally from waiting list
            assertTrue(event.getWaitingList().contains(id));
        }
        // Test the updated status after a valid draw
        assertEquals(Event.Status.DRAWN, event.getStatus());
    }

    @Test
    public void testAutoDraw_AfterDecline() {
        long now = System.currentTimeMillis();

        Event event = new Event("e1", "Test Event", "",
                now + 1000000,
                now - 10000,
                now - 1000,
                "Loc",
                3,
                10,
                REG_OPEN,
                null,
                "desc",
                Event.Tag.PARTY);

        // pending = {C}; occupied = {B,C}, capacity = 3 => 1 open slot
        event.setWaitingList(new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E")));
        event.setInvitedList(new ArrayList<>(Arrays.asList("A", "B", "C")));
        event.setAttendeesList(new ArrayList<>(Collections.singletonList("B")));
        event.setCanceledList(new ArrayList<>(Collections.singletonList("A")));

        // Test if it's valid time period
        assertTrue(event.isRegEnd());
        assertFalse(event.isEventStarted());

        // Run the logic
        MockEventRepository.runAutoDrawLogic(event);

        // Invited list should now have A,B,C plus one of D/E
        assertEquals(4, event.getInvitedList().size());
        List<String> newInvites = new ArrayList<>(event.getInvitedList());
        newInvites.removeAll(Arrays.asList("A", "B", "C"));

        // Test if the new winner is one of D/E
        assertEquals(1, newInvites.size());
        assertTrue(Arrays.asList("D", "E").contains(newInvites.get(0)));
    }

}

