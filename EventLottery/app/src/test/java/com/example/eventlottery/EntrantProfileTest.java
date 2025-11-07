package com.example.eventlottery;

import static org.junit.Assert.assertEquals;

import com.example.eventlottery.model.Profile;

import org.junit.Test;

public class EntrantProfileTest {
    @Test
    public void testUpdatePersonalInfo() {
        Profile entrant = new Profile("LOL player", "lol@gmail.com", "911");
        entrant.updatePersonalInfo("Valorant player", "val@gmail.com", "");

        assertEquals("Valorant player", entrant.getName());
        assertEquals("val@gmail.com", entrant.getEmail());
        assertEquals("", entrant.getPhone());
    }

}
