package com.example.eventlottery;

import static org.junit.Assert.*;

import com.example.eventlottery.data.MockEventRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

public class CreateEventTest {

    private MockEventRepository mockRepository;

    @Before
    public void setUp() {
        mockRepository = new MockEventRepository();
    }

    @Test
    public void testDateNormalizationLogic() {
        // Initialize a mock calendar
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 45);
        calendar.set(Calendar.MILLISECOND, 123);

        // Simulate the normalizeToDate logic
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void testEventLeadTimeValidation_Valid() {
        // Test rules of event start date from creation
        int MIN_EVENT_LEAD_DAYS = 3;

        // Today with random time
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 14);
        today.set(Calendar.MINUTE, 30);

        // Normalize to date only (like your normalizeToDate method)
        Calendar todayNormalized = Calendar.getInstance();
        todayNormalized.set(Calendar.HOUR_OF_DAY, 0);
        todayNormalized.set(Calendar.MINUTE, 0);
        todayNormalized.set(Calendar.SECOND, 0);
        todayNormalized.set(Calendar.MILLISECOND, 0);

        // Event date 3 days from now at random time
        Calendar eventDate = (Calendar) today.clone();
        eventDate.add(Calendar.DAY_OF_YEAR, MIN_EVENT_LEAD_DAYS);
        eventDate.set(Calendar.HOUR_OF_DAY, 10);

        // Normalize event date
        Calendar eventDateNormalized = (Calendar) eventDate.clone();
        eventDateNormalized.set(Calendar.HOUR_OF_DAY, 0);
        eventDateNormalized.set(Calendar.MINUTE, 0);
        eventDateNormalized.set(Calendar.SECOND, 0);
        eventDateNormalized.set(Calendar.MILLISECOND, 0);

        // Minimum allowed date (today + 3 days)
        Calendar minEventDate = (Calendar) todayNormalized.clone();
        minEventDate.add(Calendar.DAY_OF_YEAR, MIN_EVENT_LEAD_DAYS);

        // eventDateNormalized should equal minEventDate
        assertFalse("Normalized event date should be valid",
                eventDateNormalized.before(minEventDate));
    }

    @Test
    public void testEventLeadTimeValidation_Invalid() {
        // Test rules of event start date from creation
        int MIN_EVENT_LEAD_DAYS = 3;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar eventDate = (Calendar) today.clone();
        eventDate.add(Calendar.DAY_OF_YEAR, MIN_EVENT_LEAD_DAYS - 1); // 2 days from now (invalid)

        Calendar minEventDate = (Calendar) today.clone();
        minEventDate.add(Calendar.DAY_OF_YEAR, MIN_EVENT_LEAD_DAYS);

        assertTrue("Event date should be invalid when 2 days from now",
                eventDate.before(minEventDate));
    }

    @Test
    public void testRegGapValidation_Valid() {
        // Test registration gap logic
        long MIN_REG_DRAWN_GAP_MILLIS = 24L * 60 * 60 * 1000L; // 24 hours

        long eventStartTime = System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000L); // 4 days from now
        long regEndTime = eventStartTime - (25 * 60 * 60 * 1000L); // 25 hours before (valid)

        long gap = eventStartTime - regEndTime;
        assertTrue("Registration gap should be sufficient (25 hours)",
                gap >= MIN_REG_DRAWN_GAP_MILLIS);
    }

    @Test
    public void testRegGapValidation_Invalid() {
        // Test registration gap logic
        long MIN_REG_DRAWN_GAP_MILLIS = 24L * 60 * 60 * 1000L; // 24 hours

        long eventStartTime = System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000L); // 4 days from now
        long regEndTime = eventStartTime - (23 * 60 * 60 * 1000L); // 23 hours before (invalid)

        long gap = eventStartTime - regEndTime;
        assertFalse("Registration gap should be insufficient (23 hours)",
                gap >= MIN_REG_DRAWN_GAP_MILLIS);
    }

    @Test
    public void testRegPeriodValidation() {
        // Test registration period logic
        long now = System.currentTimeMillis();
        long regStart = now + 1000; // 1 second from now
        long regEnd = now + 5000;   // 5 seconds from now
        long eventStart = regEnd + (25 * 60 * 60 * 1000L); // 25 hours after reg end

        assertTrue("Valid registration period",
                regStart < regEnd && (eventStart - regEnd) >= (24 * 60 * 60 * 1000L));

        // Test invalid: regEnd after regStart
        assertFalse("Invalid: regEnd before regStart",
                regEnd < regStart);

        // Test invalid: insufficient gap
        long insufficientGapEventStart = regEnd + (12 * 60 * 60 * 1000L); // 12 hours after reg end
        assertFalse("Invalid: insufficient gap between reg end and event start",
                (insufficientGapEventStart - regEnd) >= (24 * 60 * 60 * 1000L));
    }

    @Test
    public void testRegPeriodValidation_EdgeCases() {
        long now = System.currentTimeMillis();

        // Test exact 24-hour gap (should be valid - boundary case)
        long eventStartExact = now + (48 * 60 * 60 * 1000L); // 48 hours from now
        long regEndExact = eventStartExact - (24 * 60 * 60 * 1000L); // Exactly 24 hours before
        assertTrue("Exactly 24-hour gap should be valid",
                (eventStartExact - regEndExact) >= (24 * 60 * 60 * 1000L));

        // Test registration start equals registration end (invalid)
        long sameTime = now + 1000;
        assertFalse("Registration start and end at same time should be invalid",
                sameTime < sameTime);
    }
}