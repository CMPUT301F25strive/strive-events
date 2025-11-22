package com.example.eventlottery;

import static com.example.eventlottery.model.Event.Status.REG_OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

/**
 * This is a test class for the event
 */

public class EventTest {
    Profile userProfile_1 = new Profile("1BC123456789", "John Doe", "johndoe@example.com", true);
    Profile userProfile_2 = new Profile("abcdefghijklmn", "Guess Whom", "whoamI@ualberta.ca", true);
    // GOAT
    Event event = new Event("1", "Valorant Tournament", "Tenz",
            1024, 526, 886, "Los Angeles", 2, 2, REG_OPEN, "", "A Valorant Tournament hosted by Tenz for a prize of $1000", Event.Tag.PARTY);


    /**
     * This tests the class will successfully create an event with correct event details.
     */
    @Test
    public void testEventCreation() {
        assertEquals("1", event.getId());
        assertEquals("Valorant Tournament", event.getTitle());
        assertEquals("A Valorant Tournament hosted by Tenz for a prize of $1000", event.getDescription());
        assertEquals("Los Angeles", event.getVenue());
        assertEquals(2, event.getCapacity());
        assertEquals(2, event.getWaitingListSpots());
        assertEquals(REG_OPEN, event.getStatus());
        assertEquals("Tenz", event.getOrganizerName());
        assertEquals("", event.getPosterUrl());
        assertEquals(0, event.getWaitingListSize());
        assertEquals(0, event.getAttendeesListSize());
    }

    /**
     * This tests the function of joining and leaving waiting list
     */
    @Test
    public void testWaitingList() {
        event.joinWaitingList(userProfile_1.getDeviceID());
        assertEquals(1, event.getWaitingListSize());
        assertTrue(event.isOnWaitingList(userProfile_1.getDeviceID()));

        // Test multiple entrants joined
        event.joinWaitingList(userProfile_2.getDeviceID());
        assertEquals(2, event.getWaitingListSize());
        assertTrue(event.isOnWaitingList(userProfile_2.getDeviceID()));

        // Test no duplicates allowed
        event.joinWaitingList(userProfile_1.getDeviceID());
        assertEquals(2, event.getWaitingListSize());
        assertTrue(event.isOnWaitingList(userProfile_1.getDeviceID()));

        event.leaveWaitingList(userProfile_1.getDeviceID());
        assertEquals(1, event.getWaitingListSize());
        assertFalse(event.isOnWaitingList(userProfile_1.getDeviceID()));
    }

    /**
     * This tests the function of join and leaving attendees list.
     */
    @Test
    public void testAttendeesList() {
        event.joinAttendeesList(userProfile_1.getDeviceID());
        assertEquals(1, event.getAttendeesListSize());
        assertTrue(event.isOnAttendeesList(userProfile_1.getDeviceID()));

        // Test multiple entrants joined
        event.joinAttendeesList(userProfile_2.getDeviceID());
        assertEquals(2, event.getAttendeesListSize());
        assertTrue(event.isOnAttendeesList(userProfile_2.getDeviceID()));

        // Test no duplicates allowed
        event.joinAttendeesList(userProfile_1.getDeviceID());
        assertEquals(2, event.getAttendeesListSize());
        assertTrue(event.isOnAttendeesList(userProfile_1.getDeviceID()));

        event.leaveAttendeesList(userProfile_1.getDeviceID());
        assertEquals(1, event.getAttendeesListSize());
    }

    /**
     * This tests the function of change in staus    (To be continued)
     */
    @Test
    public void testRefreshStatus() {
        long now = System.currentTimeMillis();
        long oneHour = 60L * 60 * 1000;
        long twoHours = 2 * oneHour;

        long eventStart = now + twoHours;     // event 2h from now
        long regStart = now - oneHour;     // reg started 1h ago
        long regEnd = now + oneHour;     // reg ends 1h from now

        Event event = new Event(
                "1",
                "Valorant Tournament",
                "Tenz",
                eventStart,
                regStart,
                regEnd,
                "LA",
                2,
                2,
                Event.Status.REG_OPEN,
                "",
                "A Valorant Tournament hosted by Tenz for a prize of $1000",
                Event.Tag.PARTY
        );

        // "now" ∈ (-∞, regStart)
        event.setRegStartTimeMillis(now + oneHour);   // reg starts 1h in future
        event.setRegEndTimeMillis(now + twoHours);    // reg ends 2h in future
        event.refreshStatus();
        assertTrue(event.isRegClosed());
        assertEquals(Event.Status.REG_CLOSED, event.getStatus());

        // "now" ∈ [regStart, regEnd]
        event.setRegStartTimeMillis(now - oneHour);   // reg started 1h ago
        event.setRegEndTimeMillis(now + oneHour);     // reg ends 1h later
        event.refreshStatus();
        assertTrue(event.isRegOpen());
        assertEquals(Event.Status.REG_OPEN, event.getStatus());

        // "now" ∈ [regStart, regEnd] AND full waiting list
        event.setWaitingListSpots(1);
        event.getWaitingList().clear();
        event.joinWaitingList("user1");
        event.refreshStatus();
        assertTrue(event.isRegClosed());

        // For DRAWN (To be continued)

        // "now" ∈ [eventStart, +∞) → FINALIZED
        event.setEventStartTimeMillis(now - oneHour); // event started 1h ago
        event.setRegStartTimeMillis(now - twoHours);  // reg started 2h ago
        event.setRegEndTimeMillis(now - oneHour);     // reg ended 1h ago
        event.refreshStatus();
        assertTrue(event.isFinalized());
        assertEquals(Event.Status.FINALIZED, event.getStatus());
    }

    /**
     * This tests the function of total waiting list spots
     */
    @Test
    public void testWaitingListSpots() {
        Event event = new Event(
                "unlimited-test",
                "Unlimited Test Event",
                "Organizer",
                13,
                11,
                12,
                "Test Venue",
                2,
                2,  // Initially
                REG_OPEN,
                "poster_url",
                "Test Description",
                Event.Tag.MUSIC
        );
        // Not full
        event.joinWaitingList("user1");
        assertFalse(event.isWaitingListFull());

        // Full
        event.joinWaitingList("user2");
        assertTrue(event.isWaitingListFull());

        // Reset as unlimited spots, so not full
        event.setWaitingListSpots(-1);
        assertFalse(event.isWaitingListFull());
    }
}
