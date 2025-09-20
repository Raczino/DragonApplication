package com.raczkowski.app.surveys.survey;

import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@NoArgsConstructor
public final class SurveyTime {
    public static boolean isActive(ZonedDateTime scheduledFor, ZonedDateTime endTime) {
        if (endTime == null) return false;
        if (scheduledFor != null && endTime.isBefore(scheduledFor)) return false;

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        boolean hasStarted = (scheduledFor == null) || !now.isBefore(scheduledFor);
        boolean notEnded = now.isBefore(endTime);
        return hasStarted && notEnded;
    }
}
