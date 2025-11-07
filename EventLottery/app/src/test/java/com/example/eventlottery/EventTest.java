package com.example.eventlottery;

import static org.junit.Assert.assertEquals;

import com.example.eventlottery.model.Event;

import org.junit.Test;

public class EventTest {
    @Test
    public void testEventCreation() {
        Event event = new Event("1", "Valorant Tournament", "Tenz", 0, "Los Angeles", 200, 200, Event.Status.REG_OPEN, 1, "A Valorant Tournament hosted by Tenz for a prize of $1000");
        assertEquals("1", event.getId());
        assertEquals("Valorant Tournament", event.getTitle());
        assertEquals("A Valorant Tournament hosted by Tenz for a prize of $1000", event.getDescription());
        assertEquals("Los Angeles", event.getVenue());
        assertEquals(200, event.getCapacity());
        assertEquals(200, event.getSpotsRemaining());
        //assertEquals(REG_OPEN, event.getStatus()); don't know how to test enums
        assertEquals("Tenz", event.getOrganizerName());
        assertEquals(1, event.getPosterResId());
        //assertNull(event.getWaitingList());
    }
}
