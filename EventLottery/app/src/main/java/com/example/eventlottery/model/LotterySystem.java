package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is a class representing the Lottery System.
 */

public class LotterySystem {
    /**
     * This is the drawRounds function. It randomly select the winners from the waiting list.
     * @param pool the waiting list containing a list of deviceID.
     * @param number the number to be drawn from the pool
     * @return a list of deviceID that wins the lottery.
     */
    public  static List<String> drawRounds(List<String> pool, int number){
        // If the number is larger than the size of pool,
        // all the people in the pool will be selected.
        if (pool.size() <= number){
            return pool;
        }

        Random random = new Random();
        List<String> winners = new ArrayList<>();

        // Keep sampling winners until reaching the desired number
        while (winners.size() < number){
            int index = random.nextInt(pool.size());
            String winner = pool.get(index);

            // Invite the winner who hasn't been invited
            if (!winners.contains(winner)){
                winners.add(winner);
            }
        }
        return winners;
    }
}
