package com.authmat.tool.events;

public record PublicKeyRotationEvent(String issuer, String publicKey, String kid) {
}
