package com.payme.internal.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to obtain a service token using a service's name or identifier.
 * <p>
 * Typically used in inter-service authentication scenarios where a system component requests secure access credentials.
 */
public record ServiceTokenRequestDto(
        @NotBlank(message = "Service name or ID must not be blank.")
        @Size(min = 4, message = "Service name or ID must be at least 4 characters long.")
                        String serviceNameOrId
){}
