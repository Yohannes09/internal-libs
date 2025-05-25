package com.payme.internal.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
/**
 * Represents a request for generating a user token using the user's identifier and associated roles.
 * <p>
 *
 * @param usernameOrId A unique identifier such as username or user ID.
 * @param roles        A set of roles or scopes that define the user's access privileges.
 */
public record UserTokenRequestDto(
        @Size(min = 6, message = "Username or ID must be at least 6 characters long")
        @NotBlank(message = "A username or ID are required.")
        String usernameOrId,

        @NotEmpty(message = "Must include at least 1 role or scope")
        Set<
                @NotBlank(message = "Roles must not be null or blank. ")
                @Size(min = 4, message = "Roles must at least 4 characters long. ")
                        String> roles
){}
