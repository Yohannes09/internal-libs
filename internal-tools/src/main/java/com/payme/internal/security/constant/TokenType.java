package com.payme.internal.security.constant;

public enum TokenType {
    ACCESS("ACCESS"),
    REFRESH("REFRESH"),
    INITIALIZATION("INITIALIZATION");

    private final String tokenType;

    TokenType(String tokenType){
        this.tokenType = tokenType;
    }
}
