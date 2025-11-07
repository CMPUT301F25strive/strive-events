package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class LotterySystem {

    /**
     * Draws winners from an event's waiting list based on the event's capacity
     * @param waitingList: the waiting list of the event
     * @param capacity: total space the event has
     * @return list of winners from the waiting list with the size of the capacity
     */
    public  static List<String> drawRounds(List<String> waitingList, int capacity){
        if (waitingList.size() <= capacity){
            return waitingList;
        }

        Random random = new Random();
        List<String> winners = new ArrayList<>();

        while (winners.size() < capacity){
            int index = random.nextInt(waitingList.size());
            String winner = waitingList.get(index);

            if (!winners.contains(winner)){
                winners.add(winner);
            }
        }
        return winners;
    }

    /**
     * Draw an additional winner from an event's waiting after a user declines after initially winning
     * @param waitingList: the waiting list of the event
     * @param currentWinners: the list of winners with declined winner
     * @param declinedWinner : the deviceId of the user that declined
     * @return list of winners with declined winner removed and new winner added
     */
    public static List<String> drawReplacement(List<String> waitingList, List<String> currentWinners, String declinedWinner) {
        List<String> updatedWinners = new ArrayList<>(currentWinners);
        updatedWinners.remove(declinedWinner);

        List<String> availableEntrants = new ArrayList<>(waitingList);
        availableEntrants.removeAll(updatedWinners);
        availableEntrants.remove(declinedWinner);

        if (availableEntrants.isEmpty()) {
            System.out.println("No available entrants.");
            return updatedWinners;
        }

        Random random = new Random();
        String replacement = availableEntrants.get(random.nextInt(availableEntrants.size()));

        updatedWinners.add(replacement);

        return updatedWinners;
    }

}
