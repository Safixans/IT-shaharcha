package com.itshaharcha.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Portfolio visibility (spec Portfolio.visibility: [private, unlisted, public]).
 *
 * <p>{@code private} is a Java keyword, so the constants are uppercase and the
 * lowercase wire form is handled via Jackson {@link JsonValue}/{@link JsonCreator}.
 * Persisted as the constant name (uppercase) by {@code @Enumerated(STRING)}.
 */
public enum Visibility {
    PRIVATE,
    UNLISTED,
    PUBLIC;

    @JsonValue
    public String wire() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Visibility fromWire(String value) {
        if (value == null) {
            return null;
        }
        return Visibility.valueOf(value.trim().toUpperCase());
    }
}
