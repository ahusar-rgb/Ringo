package com.ringo.model.enums;

import lombok.Getter;

@Getter
public enum OrganisationType {
    INDIVIDUAL("individual"),
    COMPANY("company"),
    NON_PROFIT("non_profit");

    private final String value;

    OrganisationType(String value) {
        this.value = value;
    }
}
