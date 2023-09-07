package com.ringo.service.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Time {
    public static LocalDateTime getLocalUTC() {
        Instant instant = Instant.now();
        return LocalDateTime.from(instant.atZone(ZoneOffset.UTC));
    }
}
