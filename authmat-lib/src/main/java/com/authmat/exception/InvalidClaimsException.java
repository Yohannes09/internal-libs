package com.authmat.exception;

public class InvalidClaimsException extends RuntimeException {
    public InvalidClaimsException(String message) {
        super(message);
    }
}
