package com.payme.internal.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        LocalDateTime errorTimestamp,
        String message,
        int statusCode,
        String requestPath
) {}
