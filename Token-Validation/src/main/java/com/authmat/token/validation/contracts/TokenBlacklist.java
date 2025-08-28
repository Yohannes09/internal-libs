package com.authmat.token.validation.contracts;
@FunctionalInterface
public interface TokenBlacklist {
    boolean isBlacklisted(String token);
}
