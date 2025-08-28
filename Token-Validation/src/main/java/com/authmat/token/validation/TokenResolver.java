package com.authmat.token.validation;

import com.authmat.token.validation.contracts.TokenBlacklist;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
public final class TokenResolver {
    public <T> Optional<T> resolveClaim(String token, Key publicKey, Function<Claims, T> claimsResolver){

        final Claims claims = extractAllClaims(token, publicKey);
        T claim = claimsResolver.apply(claims);

        boolean isNull = (claim == null);
        boolean isEmptyClaim =
                (claim instanceof String stringClaim && stringClaim.isEmpty()) ||
                (claim instanceof List<?> listClaim && listClaim.isEmpty()) ||
                (claim instanceof Map<?,?> mapClaim && mapClaim.isEmpty());

        return !isNull && !isEmptyClaim ?
                Optional.of(claim) : Optional.empty();
    }

    public JwsHeader<?> resolveHeader(String token){
        return Jwts.parserBuilder()
                .build()
                .parseClaimsJws(token)
                .getHeader();
    }

    public boolean isTokenValid(String token, Key publicKey){
        return !extractSubject(token, publicKey).isBlank() &&
                !extractAudience(token, publicKey).isBlank() &&
                !extractAuthorities(token, publicKey).isEmpty() &&
                !isTokenExpired(token, publicKey);
    }

    public boolean isTokenValid(
            String token, Key publicKey, String requiredSubject, String requiredAudience
    ){
        return requiredSubject.equals(extractSubject(token, publicKey)) &&
                requiredAudience.equals(extractAudience(token, publicKey)) &&
                !extractAuthorities(token, publicKey).isEmpty() &&
                !isTokenExpired(token, publicKey);
    }

    public boolean isTokenValid(
            String token,
            Key publicKey,
            String requiredSubject,
            String requiredAudience,
            Set<String> requiredAuthorities
    ){
        boolean isAuthorized = requiredAuthorities
                .stream()
                .anyMatch(extractAuthorities(token, publicKey)::contains);

        return requiredSubject.equals(extractSubject(token, publicKey)) &&
                requiredAudience.equals(extractAudience(token, publicKey)) &&
                !isTokenExpired(token, publicKey) &&
                isAuthorized;
    }

    public Optional<String> extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.TOKEN_HEADER.getValue());

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX.getValue())) {
            return Optional.of(
                    bearerToken.substring(SecurityConstants.TOKEN_PREFIX.getValue().length())
            );
        }

        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) return Optional.of(tokenParam);

        return Optional.empty();
    }

    public String extractSubject(String token, Key publicKey){
        return resolveClaim(token, publicKey, Claims::getSubject)
                .orElse("");
    }

    public String extractAudience(String token, Key publicKey){
        return resolveClaim(token, publicKey, Claims::getAudience)
                .orElse("");
    }

    public String extractIssuer(String token, Key publicKey){
        return resolveClaim(token, publicKey, Claims::getIssuer)
                .orElse("");
    }

    public Set<String> extractAuthorities(String token, Key publicKey){
        List<String> authorities = resolveClaim(
                token,
                publicKey,
                claims -> claims.get("authorities", List.class))
                .stream()
                .map(String.class::cast)
                .toList();

        return new HashSet<>(authorities);
    }

    public Instant extractExpiration(String token, Key publicKey){
        return resolveClaim(token, publicKey, Claims::getExpiration)
                .map(Date::toInstant)
                .orElse(Instant.now());
    }

    public boolean isTokenExpired(String token, Key publicKey){
        return extractExpiration(token, publicKey)
                .isBefore(Instant.now());
    }

    public PublicKey loadPublicKey(String publicKey, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] decodedKey = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(decodedKey);

        return KeyFactory
                .getInstance(algorithm)
                .generatePublic(encodedKeySpec);
    }

    private static Claims extractAllClaims(String token, Key publicKey){
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}