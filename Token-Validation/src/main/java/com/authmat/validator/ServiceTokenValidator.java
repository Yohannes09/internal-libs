package com.authmat.validator;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@RequiredArgsConstructor
public class ServiceTokenValidator implements TokenValidator {

    @Override
    public boolean isTokenValid(
            String token,
            String signingKey,
            String signingAlgorithm
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return false;
//        return hasValidType(token, signingKey, requiredClaimTypes, signingAlgorithm) &&
//                hasValidRoles(token, signingKey, requiredRoles, signingAlgorithm) &&
//                hasValidAudience(token, signingKey, signingAlgorithm) &&
//                hasValidSubject(token, signingKey, signingAlgorithm);
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
            String audience,
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

    private boolean hasValidType() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return false;
    }

    public boolean hasValidRoles() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Have to rework
        return false;
    }

}

