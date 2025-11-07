package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is a class representing as the lottery System. It has 2 functions.
 * 1. draw the initial lottery winners
 * 2. draw replacement lottery when a user declines the invitation.
 */

public class LotterySystem {
    /**
     * This is the drawRounds function. It randomly select the winners from the waiting list.
     * @param waitingList the waiting list containing a list of deviceID.
     * @param capacity the number to be drawn from the pool
     * @return a list of deviceID that wins the lottery.
     */
    public  static List<String> drawRounds(List<String> waitingList, int capacity){

        // If the number of capacity is larger than the number of waiting list, all the people in the waiting list will be selected.
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
     * This is the class that draws a replacement when an entrant on the winners list declined, so one spot will open up for another user.
     * @param waitingList the waiting list of the event.
     * @param currentWinners the winners from the first draw.
     * @param declinedWinner the user that declines the invitation
     * @return a list of new winners list, with the declined user removed, and new entrants added.
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
