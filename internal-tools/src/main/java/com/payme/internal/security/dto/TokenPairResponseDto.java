package com.payme.internal.security.dto;

/**
 * Represents a pair of JWT tokens returned after successful authentication or token refresh.
 * <p>
 * Contains:
 * <ul>
 *   <li>{@code accessToken} — short-lived token used to access protected resources</li>
 *   <li>{@code refreshToken} — long-lived token used to obtain new access tokens</li>
 * </ul>
 */
public record TokenPairResponseDto(
        String accessToken,
        String refreshToken
){}
