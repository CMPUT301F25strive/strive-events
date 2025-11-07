package com.example.eventlottery;

import static org.junit.Assert.assertEquals;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.LotterySystem;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LotterySystemTest {
    Event event = new Event("1", "Valorant Tournament", "Tenz", 0,
            "Los Angeles", 3, 200, Event.Status.REG_OPEN, 1,
            "A Valorant Tournament hosted by Tenz for a prize of $1000");

    Profile userProfile_1 = new Profile("1BC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890");
    Profile userProfile_2 = new Profile("2BC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890");
    Profile userProfile_3 = new Profile("3BC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890");
    Profile userProfile_4 = new Profile("4BC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890");
    Profile userProfile_5 = new Profile("5BC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890");



    @Test
    public void testLotteryDraw(){
        event.joinWaitingList(userProfile_1.getDeviceID());
        event.joinWaitingList(userProfile_2.getDeviceID());
        event.joinWaitingList(userProfile_3.getDeviceID());
        event.joinWaitingList(userProfile_4.getDeviceID());
        event.joinWaitingList(userProfile_5.getDeviceID());
        List<String> winners_list = LotterySystem.drawRounds(event.getWaitingList(), event.getCapacity());
        assertEquals(event.getCapacity(),winners_list.size());

        // check for non-duplicates
        Set<String> uniqueWinners = new HashSet<>(winners_list);
        assertEquals(winners_list.size(), uniqueWinners.size());

    }
}
