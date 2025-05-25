package com.payme.internal.security.component.PublicKeyResolver;

import lombok.Builder;

/**
 * Immutable container for cryptographic metadata used in JWT token validation.
 *
 * <p>This record encapsulates all necessary public key information required by
 * a service to independently validate JWTs issued by an external token service.</p>
 *
 * <p>It includes the current and previous public keys to support key rotation,
 * along with the associated signing algorithm and expected token issuer. This
 * structure is designed to be safely shared, cached, or injected across services
 * that need to perform signature verification without directly coupling to the
 * {@code PublicKeyResolver}'s internal model.</p>
 *
 * <p>Instances are constructed via a builder to ensure clarity and immutability.</p>
 *
 * @see PublicKeyResolver
 */
@Builder
public record PublicKeyMetaData(
        String currentPublicKey,
        String previousPublicKey,
        String signingAlgorithm,
        String tokenIssuer
){}
