package com.authmat.tool.events;

import java.time.Instant;

public record NewUserEvent(Object id, String username, String email, Instant createdAt) {
}
