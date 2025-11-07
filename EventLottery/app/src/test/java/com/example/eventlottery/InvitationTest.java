package com.example.eventlottery;

import static org.junit.Assert.assertThrows;

import com.example.eventlottery.data.MockEventRepository;
import com.example.eventlottery.data.MockProfileRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.InvitationService;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class InvitationTest {

    private Profile mockProfile(String id, String name, String email, String phone) {
        return new Profile(id, name, email, "");
    }

    private Event mockEvent() {
        return new Event("1", "Valorant", "Tenz", 0,"Home", 200, 200, Event.Status.REG_OPEN, 1, "Tournament");
    }

    @Test
    public void testSendInvitations() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        MockEventRepository eventRepo = new MockEventRepository();
        Event event1 = mockEvent();
        eventRepo.add(event1);
        profileRepo.saveUser(mockProfile("123123", "Tyson", "tyson3@example.com", "12312312313"));
        profileRepo.saveUser(mockProfile("098098", "Bobby", "bobby@example.com", "1230987654"));
        profileRepo.saveUser(mockProfile("555555", "random", "random@example.com", "12425234223"));
        InvitationService invitationService = new InvitationService(eventRepo, profileRepo);
        List<String> winners = Arrays.asList("123123", "555555");
        invitationService.sendInvitations(winners, "1");
        System.out.println("Invitations sent successfully to all winners.");
    }

    @Test
    public void testSendInvitationsException() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        MockEventRepository eventRepo = new MockEventRepository();
        Event event1 = mockEvent();
        eventRepo.add(event1);
        profileRepo.saveUser(mockProfile("123123", "Tyson", "tyson3@example.com", "12312312313"));
        InvitationService invitationService = new InvitationService(eventRepo, profileRepo);
        List<String> winners = Arrays.asList("123123", "99999");

        assertThrows(NoSuchElementException.class, () -> {
            invitationService.sendInvitations(winners, "1");
        });
        System.out.println("Exception thrown correctly for missing winner.");
    }
}