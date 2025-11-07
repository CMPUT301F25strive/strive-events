package com.example.eventlottery;

import static com.example.eventlottery.model.Event.Status.REG_OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.eventlottery.model.Event;

import org.junit.Test;

/**
 * This is a test class for the event
 */

public class EventTest {
    /**
     * This tests the class will successfully create an event with correct event details.
     */
    @Test
    public void testEventCreation() {
        Event event = new Event("1", "Valorant Tournament", "Tenz", 0, "Los Angeles", 200, 200, REG_OPEN, 1, "A Valorant Tournament hosted by Tenz for a prize of $1000");
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
    }
}
