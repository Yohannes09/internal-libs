package com.authmat.events;

import java.time.Instant;
import java.util.UUID;

public record NewUserEvent(
        UUID    eventId,
        String  eventType,
        String aggregateType,
        int     schemaVersion,
        UUID    userId,
        String  username,
        String  email,
        Instant occurredAt
) {
    private static final String EVENT_TYPE = "user.created";
    private static final String AGGREGATE_TYPE = "User";
    private static final int SCHEMA_VERSION = 1;

    // The compiler auto-generates the canonical constructor that assigns all fields.
    // The compact constructor is a special record syntax that lets you
    // intercept that process to run validation before assignment happens,
    // without rewriting the assignment boilerplate yourself:
    public NewUserEvent{
        if (eventId    == null) throw new IllegalArgumentException("eventId must not be null");
        if (userId     == null) throw new IllegalArgumentException("userId must not be null");
        if (username   == null || username.isBlank()) throw new IllegalArgumentException("username must not be blank");
        if (email      == null || email.isBlank())    throw new IllegalArgumentException("email must not be blank");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt must not be null");
    }

    public static NewUserEvent of(UUID userId, String username, String email){
        return new NewUserEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                AGGREGATE_TYPE,
                SCHEMA_VERSION,
                userId,
                username,
                email,
                Instant.now()
        );
    }
}
