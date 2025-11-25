package com.example.eventlottery;

import static org.junit.Assert.*;

import com.example.eventlottery.model.EventTimeValidator;
import com.google.common.base.StandardSystemProperty;

import org.junit.Test;

import java.util.Calendar;

public class EventTimeValidatorTest {

    /**
     * This tests the function of normalizing the calendar to date-only.
     */
    @Test
    public void testDateNormalizationLogic() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 45);
        calendar.set(Calendar.MILLISECOND, 123);

        EventTimeValidator.normalizeToDate(calendar);

        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    /**
     * This tests for a valid event start date as the event starts at least MIN_EVENT_LEAD_DAYS from today
     */
    @Test
    public void testEventLeadTimeValidation_Valid() {
        Calendar today = Calendar.getInstance();
        Calendar eventDate = (Calendar) today.clone();
        eventDate.add(Calendar.DAY_OF_YEAR, EventTimeValidator.MIN_EVENT_LEAD_DAYS);

        // Should be true
        assertTrue(EventTimeValidator.isEventDateValid(today, eventDate));
    }

    /**
     * This tests an invalid event start date as the event starts less than MIN_EVENT_LEAD_DAYS from today
     */
    @Test
    public void testEventLeadTimeValidation_Invalid() {
        Calendar today = Calendar.getInstance();
        Calendar eventDate = (Calendar) today.clone();
        eventDate.add(Calendar.DAY_OF_YEAR, EventTimeValidator.MIN_EVENT_LEAD_DAYS - 1);    // Start 1 day in advance

        // Should be false
        assertFalse(EventTimeValidator.isEventDateValid(today, eventDate));
    }

    /**
     * This tests a valid gap between registration end and event start,
     * as the event starts in more than MIN_REG_DRAWN_GAP_MILLIS after registration end
     */
    @Test
    public void testRegGapValidation_Valid() {
        long eventStartTime = System.currentTimeMillis() + (4 * 24L * 60 * 60 * 1000L); // Start in 4 days
        // Reg end by (minGap + 1h) before event start
        long regEndTime = eventStartTime - EventTimeValidator.MIN_REG_DRAWN_GAP_MILLIS - 60L * 60 * 1000L;

        // Should be true
        assertTrue(EventTimeValidator.isRegPeriodValid(System.currentTimeMillis(), regEndTime, eventStartTime));
    }

    /**
     * This tests an invalid gap between registration end and event start,
     * as the event starts in less than MIN_REG_DRAWN_GAP_MILLIS after registration end
     */
    @Test
    public void testRegGapValidation_Invalid() {
        long eventStartTime = System.currentTimeMillis() + (4 * 24L * 60 * 60 * 1000L);
        // Reg end in (minGap - 1h) before event start
        long regEndTime = eventStartTime - EventTimeValidator.MIN_REG_DRAWN_GAP_MILLIS + 60L * 60 * 1000L;

        // Should be false
        assertFalse(EventTimeValidator.isRegPeriodValid(System.currentTimeMillis(), regEndTime, eventStartTime));
    }

    /**
     * This tests an invalid registration period when the registration end is before the registration start
     */
    @Test
    public void testRegPeriod_Invalid_regStartAfterEnd() {
        long now = System.currentTimeMillis();
        long regStart = now + 5000;
        long regEnd = now + 1000; // end before start (invalid)
        long eventStart = now + (2 * 24L * 60 * 60 * 1000L);

        // Should be false
        assertFalse(EventTimeValidator.isRegPeriodValid(regStart, regEnd, eventStart));
    }


    /**
     * This tests an exact minimum gap between registration end and event start,
     */
    @Test
    public void testRegGapValidation_ExactBoundary() {
        long now = System.currentTimeMillis();
        long eventStartTime = now + (3 * 24L * 60 * 60 * 1000L);
        long regEndTime = eventStartTime - EventTimeValidator.MIN_REG_DRAWN_GAP_MILLIS;  // Exactly 24h

        // Should be true
        assertTrue(EventTimeValidator.isRegPeriodValid(now, regEndTime, eventStartTime));
    }

}
