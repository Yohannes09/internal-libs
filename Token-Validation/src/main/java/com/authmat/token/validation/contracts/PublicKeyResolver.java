package com.authmat.token.validation.contracts;

import java.util.Optional;

@FunctionalInterface
public interface PublicKeyResolver {
    Optional<String> resolve(String kid);
}
