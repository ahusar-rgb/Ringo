package com.ringo.mock.dto;

import com.ringo.config.Constants;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.it.util.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EventDtoMock {

    public static EventRequestDto getEventDtoMock() {
        return EventRequestDto.builder()
                .id(IdGenerator.getNewId())
                .name("Test")
                .isTicketNeeded(true)
                .description("Test description")
                .categoryIds(new ArrayList<>())
                .price(0.0f)
                .currencyId(1L)
                .coordinates(new Coordinates(50.0, 50.0))
                .address("Test address")
                .startTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)))
                .endTime(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)))
                .capacity(100)
                .build();
    }
}
