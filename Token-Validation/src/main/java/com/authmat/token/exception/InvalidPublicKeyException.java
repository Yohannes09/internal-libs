package com.authmat.token.exception;

public class InvalidPublicKeyException extends RuntimeException {
    public InvalidPublicKeyException(String message) {
        super(message);
    }
}
