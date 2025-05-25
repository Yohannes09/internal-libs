package com.payme.internal.security.token;

import com.payme.internal.security.constant.TokenRecipient;
import com.payme.internal.security.constant.TokenType;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@RequiredArgsConstructor
public class ServiceTokenValidator implements TokenValidator{
    private final String issuer;
    private final String audience;
    private final Set<TokenType> requiredTokenTypes;
    private final Set<TokenRecipient> requiredClaimTypes;
    private final Set<String> requiredRoles;


    @Override
    public boolean isTokenValid(
            String token,
            String signingKey,
            String signingAlgorithm
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        return hasValidType(token, signingKey, requiredClaimTypes, signingAlgorithm) &&
                hasValidRoles(token, signingKey, requiredRoles, signingAlgorithm) &&
                hasValidAudience(token, signingKey, signingAlgorithm) &&
                hasValidSubject(token, signingKey, signingAlgorithm);
    }

    private boolean hasValidType(
            String token,
            String signingKey,
            Set<TokenRecipient> allowedTypes,
            String signingAlgorithm
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Optional<String> extractedType = TokenResolver.resolveClaim(
                token,
                signingKey,
                signingAlgorithm,
                claims -> claims.get("type", String.class)
        );

        return allowedTypes.stream()
                .map(TokenRecipient::getTokenRecipient)
                .anyMatch(extractedType::equals);
    }

    public boolean hasValidRoles(
            String token,
            String signingKey,
            Set<String> requiredRoles,
            String signingAlgorithm
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        var extractedRoles = TokenResolver.resolveClaim(
                token,
                signingKey,
                signingAlgorithm,
                claims -> claims.get("roles", List.class)
        );

        if(extractedRoles.isEmpty()){
            return false;
        }

        Set<String> reqRoles = new HashSet<>(requiredRoles);

        return extractedRoles.get()
                .stream()
                .anyMatch(reqRoles::contains);
    }

    private boolean hasValidSubject(
            String token,
            String signingKey,
            String signingAlgorithm
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Optional<String> extractedSubject = TokenResolver.resolveClaim(
                token,
                signingKey,
                signingAlgorithm,
                Claims::getSubject
        );

        return extractedSubject.isPresent();
    }

    private boolean hasValidAudience(
            String token,
            String signingKey,
            String signingAlgorithm
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Optional<String> extractedAudience = TokenResolver.resolveClaim(
                token,
                signingKey,
                signingAlgorithm,
                Claims::getAudience
        );

        return extractedAudience
                .map(audience::equals)
                .orElse(false);
    }

}

