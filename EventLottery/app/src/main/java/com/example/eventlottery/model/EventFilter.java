package com.example.eventlottery.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class EventFilter implements Serializable {
    @Nullable
    private Long filterStartTimeMillis;

    @Nullable
    private Long filterEndTimeMillis;

    @Nullable
    private Event.Tag filterTag;

    public EventFilter(@Nullable Long filterStartTimeMillis, @Nullable Long filterEndTimeMillis, @Nullable Event.Tag filterTag) {
        this.filterStartTimeMillis = filterStartTimeMillis;
        this.filterEndTimeMillis = filterEndTimeMillis;
        this.filterTag = filterTag;
    }

    public EventFilter() {
        this(null, null, null);
    }

    public boolean match(Event event) {
        long eventStart = event.getStartTimeMillis();
        Event.Tag eventTag = event.getTag();

        // Filter time range
        // TODO: should reflect the status of events rather than the time of events
        // If start time of filter is set, event must be on/after it
        if (filterStartTimeMillis != null && eventStart < filterStartTimeMillis) {
            return false;
        }

        // If end time of filter is set, event must be on/before it
        if (filterEndTimeMillis != null && eventStart > filterEndTimeMillis) {
            return false;
        }

        // Filter filterTag
        if (filterTag != null && !filterTag.equals(eventTag)) {
            return false;
        }

        return true;
    }

    @Nullable
    public Long getFilterStartTimeMillis() {
        return filterStartTimeMillis;
    }

    public void setFilterStartTimeMillis(@Nullable Long filterStartTimeMillis) {
        this.filterStartTimeMillis = filterStartTimeMillis;
    }

    @Nullable
    public Long getFilterEndTimeMillis() {
        return filterEndTimeMillis;
    }

    public void setFilterEndTimeMillis(@Nullable Long filterEndTimeMillis) {
        this.filterEndTimeMillis = filterEndTimeMillis;
    }

    @Nullable
    public Event.Tag getFilterTag() {
        return filterTag;
    }

    public void setFilterTag(@Nullable Event.Tag filterTag) {
        this.filterTag = filterTag;
    }
}