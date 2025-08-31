package com.authmat.exception;

public class InvalidPublicKeyException extends RuntimeException {
    public InvalidPublicKeyException(String message) {
        super(message);
    }
}
