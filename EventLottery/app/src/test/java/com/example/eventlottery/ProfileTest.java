package com.example.eventlottery;

import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * This is a test class for Profile
 */
public class ProfileTest {
    Profile userProfile = new Profile("ABC123456789", "John Doe",
            "johndoe@example.com", "123-456-7890"
    );

    /**
     * This tests all the getter function in the profile class will get correct information.
     */
    @Test
    public void testProfile(){
        assertFalse(userProfile.isAdmin());
        assertEquals("ABC123456789",userProfile.getDeviceID());
        assertEquals("John Doe",userProfile.getName());
        assertEquals("johndoe@example.com",userProfile.getEmail());
        assertEquals("123-456-7890",userProfile.getPhone());
    }

    /**
     * This tests the update function in profile class works correctly.
     */
    @Test
    public void testUpdateProfile(){
        userProfile.updatePersonalInfo("Test_Name","test@email.com","111");
        assertEquals("Test_Name",userProfile.getName());
        assertEquals("test@email.com", userProfile.getEmail());
        assertEquals("111", userProfile.getPhone());
    }
}
