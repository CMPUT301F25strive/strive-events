package com.example.eventlottery.model;

import java.util.Calendar;

/**
 * This class provides pure date/time validation logic for creation of an event.
 *
 * Time interval rules:
 * - For event creation (in days):
 *      eventStart - now >= MIN_EVENT_LEAD_DAYS
 * - For registration period (in ms):
 *      regStart >= now
 *      regStart < regEnd
 *      defaultRegStart = now
 *      defaultRegEnd = eventStart - MIN_REG_DRAWN_GAP
 *      eventStart - regEnd >= MIN_REG_DRAWN_GAP
 */
public final class EventTimeValidator {

    // Event creation lead time: date-based
    public static final int MIN_EVENT_LEAD_DAYS = 3;   // 3 days

    // Gap between registration close and event start
    public static final int MIN_REG_DRAWN_GAP_DAYS = 1;    // Literally 1 day with 24h
    public static final long MIN_REG_DRAWN_GAP_MILLIS =
            MIN_REG_DRAWN_GAP_DAYS * 24L * 60 * 60 * 1000L;    // 1*24h in ms

    private EventTimeValidator() {}

    /**
     * This method checks if an event starts at least MIN_EVENT_LEAD_DAYS from today (date-only comparison).
     * @param today: the calendar of today
     * @param eventDate: the calendar of event start date
     * @return true if event start date is valid (>= MIN_EVENT_LEAD_DAYS); else false
     */
    public static boolean isEventDateValid(Calendar today, Calendar eventDate) {
        // Normalize today to date-only format
        Calendar todayNorm = (Calendar) today.clone();
        normalizeToDate(todayNorm);

        // Normalize event start date to date-only format
        Calendar eventNorm = (Calendar) eventDate.clone();
        normalizeToDate(eventNorm);

        // Initialize the minimum lead days
        Calendar minEventDate = (Calendar) todayNorm.clone();
        minEventDate.add(Calendar.DAY_OF_YEAR, MIN_EVENT_LEAD_DAYS);

        // Return the boolean if the event starts at least MIN_EVENT_LEAD_DAYS from today
        return !eventNorm.before(minEventDate);
    }

    /**
     * This method checks if the registration period is valid given the event start time.
     * @param regStart: the registration start time in ms
     * @param regEnd: the registration end time in ms
     * @param eventStart: the event start time in ms
     * @return true if registration period is valid; else false
     */
    public static boolean isRegPeriodValid(long regStart, long regEnd, long eventStart) {
        // If registration starts after its end or no registration period, false
        if (regStart >= regEnd) return false;

        // Return the boolean if the gap between registration end and event start time satisfies MIN_REG_DRAWN_GAP_MILLIS
        return eventStart - regEnd >= MIN_REG_DRAWN_GAP_MILLIS;
    }

    /**
     * Normalize a Calendar to date-only (time being as 00:00:00.000)
     * @param cal: the Calendar object
     */
    public static void normalizeToDate(Calendar cal) {
        // Set all time fields as 0 (except date)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
