package com.ringo.mock.model;

import com.ringo.it.util.IdGenerator;
import com.ringo.model.company.Currency;

public class CurrencyMock {

    public static Currency getCurrencyMock() {
        return Currency.builder()
                .id(IdGenerator.getNewId())
                .name("Test")
                .symbol('$')
                .build();
    }
}
