package com.example.eventlottery;

import com.example.eventlottery.data.MockProfileRepository;
import com.example.eventlottery.model.Profile;

import org.junit.Test;

public class ProfileRepositoryTest {
    private Profile mockProfile() {
        return new Profile("123123", "Tyson", "tyson3@ualberta.ca", "");
    }

    private MockProfileRepository mockProfileRepo() {
        MockProfileRepository profileRepo = new MockProfileRepository();
        profileRepo.saveUser(mockProfile());
        return profileRepo;
    }

    @Test
    public void testSaveUser() {

    }
}
