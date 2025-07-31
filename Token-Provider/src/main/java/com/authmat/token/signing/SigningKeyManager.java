package com.authmat.token.signing;

import java.security.PrivateKey;

public interface SigningKeyManager {
    PrivateKey getActiveSigningKey();
}
