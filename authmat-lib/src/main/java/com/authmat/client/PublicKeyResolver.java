package com.authmat.client;

import com.authmat.model.publickey.PublicKeyMetadata;

import java.util.Optional;

@FunctionalInterface
public interface PublicKeyResolver {
    Optional<PublicKeyMetadata> resolve(String kid);
}
