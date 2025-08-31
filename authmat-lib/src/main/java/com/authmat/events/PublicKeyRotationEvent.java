package com.authmat.events;

import lombok.Builder;

import java.time.Instant;

@Builder
public record PublicKeyRotationEvent(
        String kid,
        String publicKey,
        String signingKeyAlgorithm,
        String jwtAlgorithm,
        String issuer,
        Instant issuedAt) {
}
