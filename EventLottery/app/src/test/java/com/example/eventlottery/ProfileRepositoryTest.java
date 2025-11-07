package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.eventlottery.data.MockProfileRepository;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

import java.util.List;

/**
 * This is a test class for Profile Repository
 */
public class ProfileRepositoryTest {
    private Profile mockProfile() {
        return new Profile("123123", "Tyson", "tyson3@ualberta.ca", "123-456-7890");
    }

    private MockProfileRepository mockProfileRepo() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        profileRepo.saveUser(mockProfile());
        return profileRepo;
    }

    /**
     * This tests the profile repo stores the right profile by checking the information.
     */
    @Test
    public void testSaveUser() {
        MockProfileRepository profileRepo = mockProfileRepo();
        Profile profile = profileRepo.findUserById("123123");
        assertEquals("123123", profile.getDeviceID());
        assertEquals("Tyson", profile.getName());
        assertEquals("tyson3@ualberta.ca", profile.getEmail());
        assertEquals("123-456-7890", profile.getPhone());
    }

    /**
     * This tests the user repo will return nothing if the there's no profile in the profile repo with the specific device id.
     */
    @Test
    public void testSaveUserException() {
        MockProfileRepository profileRepo = mockProfileRepo();
        Profile profile = profileRepo.findUserById("00000000");
        assertNull(profile);
    }

    /**
     * THis tests the delete function of profile repo.
     */
    @Test
    public void testDeleteUser() {
        MockProfileRepository profileRepo = mockProfileRepo();
        Profile profile = profileRepo.findUserById("123123");
        profileRepo.deleteUser("123123");
        assertNull(profileRepo.findUserById("123123"));
    }

    /**
     * This tests the delete function will only delete the profile with given device ID.
     */
    @Test
    public void testDeleteUserException() {
        MockProfileRepository profileRepo = mockProfileRepo();
        Profile profile = profileRepo.findUserById("123123");
        profileRepo.deleteUser("000000");
        assertNotNull(profileRepo.findUserById("123123"));
    }

    /**
     * This tests the profile repo will return a list of profiles with specific role.
     */
    @Test
    public void testFindUserByRole() {
        List<Profile> profiles = mockProfileRepo().findUsersByRole(Profile.Role.USER);
        assertEquals(1, profiles.size());
    }
}
