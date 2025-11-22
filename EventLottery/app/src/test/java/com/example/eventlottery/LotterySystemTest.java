package com.example.eventlottery;

import static com.example.eventlottery.model.Event.Status.REG_OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.LotterySystem;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void testLotteryDraw() {
        event.joinWaitingList(userProfile_1.getDeviceID());
        event.joinWaitingList(userProfile_2.getDeviceID());
        event.joinWaitingList(userProfile_3.getDeviceID());
        event.joinWaitingList(userProfile_4.getDeviceID());
        event.joinWaitingList(userProfile_5.getDeviceID());
        List<String> winners_list = LotterySystem.drawRounds(event.getWaitingList(), event.getCapacity());
        assertEquals(event.getCapacity(), winners_list.size());
    }

    /**
     * This tests that the lottery system draws entrants from waiting list with no duplication.
     */
    @Test
    public void testNonDuplicates(){
        List<String> waitingList = List.of("1BC123456789", "2BC123456789","3BC123456789","4BC123456789","5BC123456789");
        List<String> winners_list = LotterySystem.drawRounds(waitingList, 3);

        Set<String> uniqueWinners = new HashSet<>(winners_list);
        assertEquals(winners_list.size(), uniqueWinners.size());
    }

    /**
     * This test the function of Draw replacement when a user choose to decline the invitation, a new entrant will be drawn from the pool.
     */
    @Test
    public  void testReplacementDraw(){
        List<String> waitingList = List.of("1BC123456789", "2BC123456789","3BC123456789","4BC123456789","5BC123456789");
        List<String> winners_list = new ArrayList<>(List.of("1BC123456789","2BC123456789","3BC123456789"));

        String decclined_entrant = "1BC123456789";

        winners_list = LotterySystem.drawReplacement(waitingList,winners_list,decclined_entrant);
        assertEquals(3, winners_list.size());
        assertFalse(winners_list.contains("1BC123456789"));

        for (String winner : winners_list) {
            assertTrue(waitingList.contains(winner));
        }
    }

    /**
     * This tests the exception that when there's no more available entrant in the pool, the winners list will remain the same.
     */
    @Test
    public void testReplacementDraw_NoMoreEntrantsInThePool(){
        List<String> waitingList = List.of("1BC123456789", "2BC123456789","3BC123456789");
        List<String> winners_list = new ArrayList<>(List.of("1BC123456789","2BC123456789","3BC123456789"));

        String declined_entrant = "1BC123456789";

        winners_list = LotterySystem.drawReplacement(waitingList,winners_list,declined_entrant);
        assertEquals(2, winners_list.size());
        assertFalse(winners_list.contains("1BC123456789"));
        assertEquals(List.of("2BC123456789", "3BC123456789"), winners_list);
    }


}
