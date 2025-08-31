package com.authmat.model.publickey;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface PublicKeyMetadata extends Serializable {
    Object getId();

    String getEncodedPublicKey();

    String getKeyAlgorithm();

    String getJwtAlgorithm();

    LocalDateTime getCreatedAt();

    LocalDateTime getExpiresAt();

    LocalDateTime getRevokedAt();
}
