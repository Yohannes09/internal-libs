package com.payme.internal.security.token;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface TokenValidator {
    boolean isTokenValid(String token, String signingKey, String signingAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException;
//    boolean hasValidClaims();
}
