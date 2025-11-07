package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

public class WaitingListTest {
    Event event = new Event("1", "Valorant Tournament", "Tenz", 0,
            "Los Angeles", 200, 200, Event.Status.REG_OPEN, 1,
            "A Valorant Tournament hosted by Tenz for a prize of $1000");
    Profile userProfile = new Profile("ABC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890"
    );
    @Test
    public void testCreatingWaitingList(){
        assertEquals(0,event.getWaitingListSize());
    }

    @Test
    public void testJoinWaitingList(){
        event.joinWaitingList(userProfile.getDeviceID());
        assertEquals(1,event.getWaitingListSize());
        assertTrue(event.isOnWaitingList(userProfile.getDeviceID()));
    }


    @Test
    public void testLeaveWaitingList(){
        event.leaveWaitingList(userProfile.getDeviceID());
        assertEquals(0,event.getWaitingListSize());
        assertFalse(event.isOnWaitingList(userProfile.getDeviceID()));
    }




}
