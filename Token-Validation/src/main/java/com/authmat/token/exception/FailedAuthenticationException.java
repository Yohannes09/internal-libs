package com.authmat.token.exception;

public class FailedAuthenticationException extends RuntimeException {
    public FailedAuthenticationException(String message) {
        super(message);
    }
}
