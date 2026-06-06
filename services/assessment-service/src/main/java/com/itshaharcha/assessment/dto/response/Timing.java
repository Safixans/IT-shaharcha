package com.itshaharcha.assessment.dto.response;

import java.time.Duration;
import java.time.Instant;

/**
 * Timezone-proof timing. The client counts down from {@code remainingSeconds} with a monotonic
 * timer and never subtracts a server timestamp from its own clock.
 */
public record Timing(Instant startedAt, Instant endsAt, Instant serverNow, long remainingSeconds) {

    public static Timing of(Instant startedAt, Instant endsAt) {
        Instant now = Instant.now();
        long remaining = Math.max(0, Duration.between(now, endsAt).getSeconds());
        return new Timing(startedAt, endsAt, now, remaining);
    }
}
