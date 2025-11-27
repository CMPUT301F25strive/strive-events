package com.example.eventlottery;

import static com.example.eventlottery.model.Event.Status.REG_OPEN;
import static org.junit.Assert.*;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.LotterySystem;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

import java.util.*;

/**
 * This is a test class for Lottery system
 */
public class LotterySystemTest {
    Event event = new Event("1", "Valorant Tournament", "Tenz",
            1024, 526, 886, "Los Angeles", 2, 2, REG_OPEN, "", "A Valorant Tournament hosted by Tenz for a prize of $1000", Event.Tag.PARTY);

    Profile userProfile_1 = new Profile("1BC123456789", "John Doe",
            "johndoe@example.com", true);
    Profile userProfile_2 = new Profile("2BC123456789", "John Doe",
            "johndoe@example.com", true);
    Profile userProfile_3 = new Profile("3BC123456789", "John Doe",
            "johndoe@example.com", true);
    Profile userProfile_4 = new Profile("4BC123456789", "John Doe",
            "johndoe@example.com", true);
    Profile userProfile_5 = new Profile("5BC123456789", "John Doe",
            "johndoe@example.com", true);


    /**
     * This tests the lottery system draws entrants from the right number of people from waiting list.
     */
    @Test
    public void testLotteryDraw_DesiredNumber() {
        event.joinWaitingList(userProfile_1.getDeviceID());
        event.joinWaitingList(userProfile_2.getDeviceID());
        event.joinWaitingList(userProfile_3.getDeviceID());
        event.joinWaitingList(userProfile_4.getDeviceID());
        event.joinWaitingList(userProfile_5.getDeviceID());
        List<String> winners_list = LotterySystem.drawRounds(event.getWaitingList(), 3);
        assertEquals(3, winners_list.size());
    }

    /**
     * This tests if the lottery system correctly handles duplicates.
     */
    @Test
    public void testLotteryDraw_Duplicates(){
        List<String> waitingList = List.of("1BC123456789", "1BC123456789","1BC123456789","1BC123456789","5BC123456789");
        List<String> winners_list = LotterySystem.drawRounds(waitingList, 2);

        Set<String> uniqueWinners = new HashSet<>(winners_list);
        assertEquals(winners_list.size(), uniqueWinners.size());
    }

    /**
     * When requested number >= pool size, drawRounds should return the entire pool.
     */
    @Test
    public void testLotteryDraw_NumberHoldsPoolSize() {
        List<String> pool = Arrays.asList(
                "1BC123456789",
                "2BC123456789",
                "3BC123456789"
        );

        List<String> winners = LotterySystem.drawRounds(pool, 10);

        // Should return all from pool
        assertEquals(pool.size(), winners.size());
        assertTrue(winners.containsAll(pool));
    }

    /**
     * When the pool is empty, drawRounds should return an empty list.
     */
    @Test
    public void testLotteryDraw_EmptyPool() {
        List<String> pool = new ArrayList<>();

        List<String> winners = LotterySystem.drawRounds(pool, 2);

        assertEquals(0, winners.size());
    }

    /**
     * When number is 0, drawRounds should return an empty list.
     */
    @Test
    public void testLotteryDraw_Zero() {
        List<String> pool = Arrays.asList(
                "1BC123456789",
                "2BC123456789",
                "3BC123456789"
        );

        List<String> winners = LotterySystem.drawRounds(pool, 0);

        assertEquals(0, winners.size());
    }
}
