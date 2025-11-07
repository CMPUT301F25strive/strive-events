package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import android.content.Context;

import com.example.eventlottery.data.MockEventRepository;
import com.example.eventlottery.data.MockProfileRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.InvitationService;
import com.example.eventlottery.model.MockInvitationService;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This is a test class for the Invitation
 */

public class InvitationTest {
    /**
     * It create a mock profile for test
     * @param id device id
     * @param name name
     * @param email email
     * @param phone phone number
     * @return a profile object
     */
    private Profile mockProfile(String id, String name, String email, String phone) {
        return new Profile(id, name, email, "");
    }

    /**
     * This creates a mock event for further testing.
     * @return a object event.
     */
    private Event mockEvent() {
        return new Event("1", "Valorant", "Tenz", 0,"Home", 200, 200, Event.Status.REG_OPEN, 1, "Tournament");
    }

    /**
     * This tests that invitation is successfully sent to people that wins the lottery.
     */
    @Test
    public void testSendInvitations() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        MockEventRepository eventRepo = new MockEventRepository();
        Event event1 = mockEvent();
        eventRepo.add(event1);
        profileRepo.saveUser(mockProfile("123123", "Tyson", "tyson3@example.com", "12312312313"));
        profileRepo.saveUser(mockProfile("098098", "Bobby", "bobby@example.com", "1230987654"));
        profileRepo.saveUser(mockProfile("555555", "random", "random@example.com", "12425234223"));
        MockInvitationService invitationService = new MockInvitationService(eventRepo, profileRepo, (Context) null);
        List<String> winners = Arrays.asList("123123", "555555");
        invitationService.sendInvitations(winners, "1");
        System.out.println("Invitations sent successfully to all winners.");
    }

    /**
     * This tests ensures that notification will rise exception if the invitation is sent to a non-exist user. (the user id is not in the profile repo)
     */
    @Test
    public void testSendInvitationsException() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        MockEventRepository eventRepo = new MockEventRepository();
        Event event1 = mockEvent();
        eventRepo.add(event1);
        profileRepo.saveUser(mockProfile("123123", "Tyson", "tyson3@example.com", "12312312313"));
        MockInvitationService invitationService = new MockInvitationService(eventRepo, profileRepo, (Context) null);
        List<String> winners = Arrays.asList("123123", "99999");

        assertThrows(NoSuchElementException.class, () -> {
            invitationService.sendInvitations(winners, "1");
        });
        System.out.println("Exception thrown correctly for missing winner.");
    }

    /**
     * Test when a user accept the invitation, it will be added to the attendess list
     */
    @Test
    public void testAcceptInvitation() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        MockEventRepository eventRepo = new MockEventRepository();
        Event event1 = mockEvent();
        eventRepo.add(event1);
        profileRepo.saveUser(mockProfile("123123", "Tyson", "tyson3@example.com", "12312312313"));
        MockInvitationService invitationService = new MockInvitationService(eventRepo, profileRepo, (Context) null);
        invitationService.acceptInvitation("123123", "1");
        assertEquals(1, event1.getAttendeesListSize());
    }

    /**
     * When a user declines an invitation, it will be removed from the waiting list, it will also not be drawn for replacement.
     */
    @Test
    public void testDeclineInvitation() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        MockEventRepository eventRepo = new MockEventRepository();
        Event event1 = mockEvent();
        eventRepo.add(event1);
        profileRepo.saveUser(mockProfile("123123", "Tyson", "tyson3@example.com", "12312312313"));
        MockInvitationService invitationService = new MockInvitationService(eventRepo, profileRepo, (Context) null);
        invitationService.declineInvitation("123123", "1");
        assertEquals(0, event1.getWaitingListSize());
    }
}