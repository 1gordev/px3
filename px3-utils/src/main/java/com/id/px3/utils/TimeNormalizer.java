package com.id.px3.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeNormalizer {

    public static Instant ceil(Instant ts, Duration timing) {
        return Instant.ofEpochMilli((ts.minus(1, ChronoUnit.MILLIS).toEpochMilli() / timing.toMillis()) * timing.toMillis()).plus(timing);
    }


}
