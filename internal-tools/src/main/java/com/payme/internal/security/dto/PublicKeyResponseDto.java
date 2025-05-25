package com.payme.internal.security.dto;

import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Nullable;

/**
 * Designed to support signing key rotation. (Base64 encoded)
 *
 * @param currentPublicKey Active public key
 * @param previousPublicKey Previous public key
 *  */
public record PublicKeyResponseDto(
        String currentPublicKey,
        @Nullable
        String previousPublicKey,
        SignatureAlgorithm signatureAlgorithm,
        String tokenIssuer
){}