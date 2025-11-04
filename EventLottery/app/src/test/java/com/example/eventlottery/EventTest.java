package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

public class EventTest {
    @Test
    public void testEventCreation() {
        Event event = new Event(1, "Valorant Tournament", "A Valorant Tournament hosted by Tenz for a prize of $1000", "Los Angeles", 200, 10.0f, 1, 1, true, new WaitingList(null)
        );
        assertEquals(1, event.getEventID());
        assertEquals("Valorant Tournament", event.getName());
        assertEquals("A Valorant Tournament hosted by Tenz for a prize of $1000", event.getDescription());
        assertEquals("Los Angeles", event.getVenue());
        assertEquals(200, event.getCapacity());
        assertEquals(10.0f,event.getPrice(), 0.001);
        assertEquals(1, event.getOrganizerID());
        assertEquals(1, event.getPosterID());
        assertNull(event.getWaitingList());
        assertTrue(event.getGeoRequired());
        assertNull(event.getWaitingList());
    }
}
