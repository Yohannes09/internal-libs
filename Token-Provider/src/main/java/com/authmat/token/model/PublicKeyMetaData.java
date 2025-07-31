package com.authmat.token.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a generic record of a public key along with its associated metadata.
 */
public interface PublicKeyMetaData extends Serializable {
    Object getId();

    String getEncodedPublicKey();

    String getSignatureAlgorithm();

    LocalDateTime getCreatedAt();

    LocalDateTime getExpiresAt();

    LocalDateTime getRevokedAt();
}
