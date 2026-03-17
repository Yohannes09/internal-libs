package com.authmat.security;
/**
 * Thrown when authority resolution fails unrecoverably.
 * Callers must deny access — never fail open.
 */
public class AuthorityResolutionException extends RuntimeException {
    public AuthorityResolutionException(String message) {
        super(message);
    }
    public AuthorityResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
