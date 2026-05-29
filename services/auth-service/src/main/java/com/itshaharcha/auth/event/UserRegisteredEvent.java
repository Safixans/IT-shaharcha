package com.itshaharcha.auth.event;

import java.time.Instant;
import java.util.UUID;

/** Emitted when a new account is created; consumed by user/notification services. */
public record UserRegisteredEvent(
        UUID accountId,
        String email,
        String username,
        Instant occurredAt) {

    public static final String TOPIC = "user.registered";
}
