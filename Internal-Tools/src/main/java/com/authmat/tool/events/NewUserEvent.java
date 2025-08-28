package com.authmat.tool.events;

public record NewUserEvent(Object id, String username, String email) {
}
