package com.payme.internal.security.model;

public record RecentPublicKeys(
        String currentPublicKey,
        String previousPublicKey
) {
}
