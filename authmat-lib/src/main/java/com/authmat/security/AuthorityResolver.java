package com.authmat.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

/**
 * AuthorityResolver
 *
 * Resolves the current granted authorities for a given userId at request time.
 *
 * Authorities are never embedded in the JWT. They are resolved live on every
 * authenticated request, ensuring revocation is reflected immediately.
 *
 * Implementations:
 *   LocalAuthorityResolver  — AuthMat: resolves directly from own service layer (Redis → DB)
 *   RemoteAuthorityResolver — DocKeep: resolves via HTTP call to AuthMat's internal authority endpoint
 *
 * Future:
 *   GrpcAuthorityResolver   — drop-in replacement for RemoteAuthorityResolver using gRPC transport.
 *                             Swap the Spring bean, nothing else changes.
 */
public interface AuthorityResolver {

    /**
     * Resolve the current set of granted authorities for the given userId.
     *
     * @param userId the authenticated subject from the JWT
     * @return non-null, possibly empty set of GrantedAuthority
     * @throws AuthorityResolutionException if the authority store is unreachable
     *         or returns an unrecoverable error. Callers should treat this as
     *         a 503 — do not grant access on resolution failure.
     */
    Set<GrantedAuthority> resolve(String userId);
}
