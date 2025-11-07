package com.example.eventlottery.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class LotterySystem {
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

}
