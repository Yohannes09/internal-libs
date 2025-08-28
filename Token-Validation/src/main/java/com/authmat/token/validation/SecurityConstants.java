package com.authmat.token.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SecurityConstants {
    TOKEN_HEADER("Authorization"),
    TOKEN_PREFIX("Bearer");

    private final String value;
}
