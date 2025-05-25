package com.payme.internal.security.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class for extracting claims from JWT tokens.
 *
 * <p>
 * {@code TokenResolver} simplifies token parsing and claim resolution,
 * ensuring null-safe and content-aware extraction.
 * </p>
 *
 * <ul>
 *   <li>Extracts claims using a provided resolver function.</li>
 *   <li>Returns {@code Optional.empty()} if the claim is {@code null} or empty.</li>
 *   <li>Supports common claim types like {@code String} and {@code List}.</li>
 * </ul>
 *
 * <p>
 * Designed for microservices — no internal logging or exception wrapping.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * Optional<String> subject = TokenResolver.resolveClaim(
 *     token,
 *     signingKey,
 *     "RSA",
 *     Claims::getSubject
 * );
 * }</pre>
 *
 */
public final class TokenResolver {

    public static <T> Optional<T> resolveClaim(
            String token,
            String signingKey,
            String signingAlgorithm,
            Function<Claims, T> claimsResolver
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        T claim = extractClaim(token, signingKey, signingAlgorithm, claimsResolver);

        boolean isNull = (claim == null);
        boolean isEmptyClaim =
                (claim instanceof String stringClaim && stringClaim.isEmpty()) ||
                (claim instanceof List<?> listClaim && listClaim.isEmpty()); // in the future this could support Map<?,?>

        return !isNull && !isEmptyClaim ?
                Optional.of(claim) : Optional.empty();
    }


    private static <T> T extractClaim(
            String token,
            String signingKey,
            String algorithm,
            Function<Claims, T> claimExtractor
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Key key = decodePublicKey(signingKey, algorithm);
        final Claims claims = extractAllClaims(token, key);
        return claimExtractor.apply(claims);
    }

    private static PublicKey decodePublicKey(
            String key,
            String algorithm
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode Base64-encoded string into a byte array
        byte[] decodedKey = Base64.getDecoder().decode(key);

        // Wrap the key bytes in an X.509 specification (standard for public keys)
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(decodedKey);

        // Generate the public key using the specified algorithm (e.g., RSA, EC)
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        return keyFactory.generatePublic(encodedKeySpec);
    }

    private static Claims extractAllClaims(String token, Key key){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}