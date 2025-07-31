package com.authmat.token.signing;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;


@RequiredArgsConstructor
public class TokenFactory {
    private final SigningKeyManager signingKeyManager;


    public String generateNewToken(
            String subject,
            String audience,
            String tokenType,
            Map<String, Object> headerParams,
            Map<String, Object> claims,
            int tokenValidityMinutes
    ){
        claims.put("type_type", tokenType);

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofMinutes(tokenValidityMinutes));
        PrivateKey privateKey = signingKeyManager.getActiveSigningKey();

        return Jwts.builder()
                .setSubject(subject)
                .setAudience(audience)
                .setHeaderParams(headerParams)
                .setClaims(claims)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(privateKey)
                .compact();
    }

    public String generateNewToken(
            Map<String, Object> headerParams,
            Map<String, Object> claims,
            int tokenValidityMinutes
    ){
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofMinutes(tokenValidityMinutes));
        PrivateKey privateKey = signingKeyManager.getActiveSigningKey();

        return Jwts.builder()
                .setHeaderParams(headerParams)
                .setClaims(claims)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(privateKey)
                .compact();
    }

}
