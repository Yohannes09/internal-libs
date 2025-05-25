package com.payme.internal.security.constant;

import lombok.Getter;

@Getter
public enum TokenRecipient {
    SERVICE("SERVICE"),
    USER("USER");

    private final String tokenRecipient;

    TokenRecipient(String tokenRecipient){
        this.tokenRecipient = tokenRecipient;
    }

}
