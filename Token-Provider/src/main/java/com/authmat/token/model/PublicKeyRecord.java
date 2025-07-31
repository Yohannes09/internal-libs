package com.payme.token.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a generic record of a public key along with its associated metadata.
 * <p>
 * This interface abstracts the essential details of a public key, including its
 * encoded representation, algorithm, and lifecycle timestamps such as creation,
 * expiration, and revocation.
 * <p>
 * Implementations of this interface can be used to manage and track the validity
 * and usage of public keys in secure systems.
 *
 * <p><b>Key Attributes:</b></p>
 * <ul>
 *   <li><b>ID:</b> Uniquely identifies the key record</li>
 *   <li><b>Public Key:</b> Base64-encoded string of the public key</li>
 *   <li><b>Algorithm:</b> Signature algorithm associated with the key</li>
 *   <li><b>Created At:</b> Timestamp when the key was generated</li>
 *   <li><b>Expires At:</b> Timestamp when the key is no longer valid</li>
 *   <li><b>Revoked At:</b> Timestamp when the key was revoked, if applicable</li>
 * </ul>
 *
 * This interface extends {@link java.io.Serializable} to support object serialization.
 */
public interface PublicKeyRecord extends Serializable {
    Object getId();

    /**
     * @return Base64-encoded Public Key.*/
    String getPublicKey();

    String getSignatureAlgorithm();

    LocalDateTime getCreatedAt();

    LocalDateTime getExpiresAt();

    LocalDateTime getRevokedAt();
}
