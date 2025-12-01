package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.eventlottery.model.NotificationLogEntry;

import org.junit.Test;

import java.util.Date;

/**
 * This is a test class for NotificationLogEntry
 */

public class NotificationLogEntryTest {
    /**
     * This is a notification log entry for testing
     */
    NotificationLogEntry logEntry = new NotificationLogEntry("1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "Test mes",
            false,
            true,
            new Date());

    /**
     * This tests the class will successfully create a notification log entry with correct details.
     */
    @Test
    public void testCreationOfNotificationLogEntry() {
        assertEquals("1", logEntry.getId());
        assertEquals("2", logEntry.getSenderId());
        assertEquals("4", logEntry.getReceiverId());
        assertEquals("5", logEntry.getEventId());
        assertEquals("6", logEntry.getEventTitle());
        assertEquals("Test mes", logEntry.getMessage());
        assertEquals("3",logEntry.getSenderName());
        assertFalse(logEntry.isFlagged());
        assertTrue(logEntry.isSystem());
        assertNotNull(logEntry.getSentAt());
    }

}



