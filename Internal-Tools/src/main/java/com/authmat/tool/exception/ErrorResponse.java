package com.authmat.tool.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        LocalDateTime errorTimestamp,
        String message,
        int statusCode,
        String requestPath,
        Object validationErrors
) {}
