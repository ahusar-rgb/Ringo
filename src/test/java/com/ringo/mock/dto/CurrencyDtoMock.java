package com.ringo.mock.dto;

import com.ringo.dto.company.CurrencyDto;
import com.ringo.it.util.IdGenerator;

public class CurrencyDtoMock {

    public static CurrencyDto getCurrencyDtoMock() {

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return CurrencyDto.builder()
                .id(IdGenerator.getNewId())
                .name("EUR")
                .symbol('€')
                .build();
    }
}
