package com.ringo.auth;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh"),
    RECOVER("recover"),
    EMAIL_VERIFICATION("emailVerification"),
    TICKET("ticket");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

}
