package com.authmat.validation.contracts;
@FunctionalInterface
public interface TokenBlacklist {
    boolean isBlacklisted(String token);
}
