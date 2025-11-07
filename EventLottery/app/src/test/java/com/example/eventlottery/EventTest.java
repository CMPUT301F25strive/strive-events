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
    Profile userProfile_1 = new Profile("1BC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890");
    Event event = new Event("1", "Valorant Tournament", "Tenz", 0, "Los Angeles", 200, 200, REG_OPEN, 1, "A Valorant Tournament hosted by Tenz for a prize of $1000");

    /**
     * This tests the class will successfully create an event with correct event details.
     */
    @Test
    public void testEventCreation() {
        assertEquals("1", event.getId());
        assertEquals("Valorant Tournament", event.getTitle());
        assertEquals("A Valorant Tournament hosted by Tenz for a prize of $1000", event.getDescription());
        assertEquals("Los Angeles", event.getVenue());
        assertEquals(200, event.getCapacity());
        assertEquals(200, event.getSpotsRemaining());
        assertEquals(REG_OPEN, event.getStatus());
        assertEquals("Tenz", event.getOrganizerName());
        assertEquals(1, event.getPosterResId());
        assertEquals(0, event.getWaitingListSize());
        assertEquals(0, event.getAttendeesListSize());
    }

    /**
     * This tests the function of joining and leaving waitinglist
     */
    @Test
    public void testWaitingList(){
        event.joinWaitingList(userProfile_1.getDeviceID());
        assertEquals(1,event.getWaitingListSize());
        assertTrue(event.isOnWaitingList(userProfile_1.getDeviceID()));
        event.leaveWaitingList(userProfile_1.getDeviceID());
        assertEquals(0,event.getWaitingListSize());
        assertFalse(event.isOnWaitingList(userProfile_1.getDeviceID()));
    }

    /**
     * This tests the function of join and leaving attendeenlist.
     */
    @Test
    public void testAttendeenList(){
        event.joinAttendeesList(userProfile_1.getDeviceID());
        assertEquals(1,event.getAttendeesListSize());
        event.leaveAttendeesList(userProfile_1.getDeviceID());
        assertEquals(0,event.getAttendeesListSize());
    }
}
