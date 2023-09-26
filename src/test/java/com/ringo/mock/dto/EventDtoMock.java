package com.ringo.mock.dto;

import com.ringo.config.Constants;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.request.EventRequestDto;
import com.ringo.dto.company.request.TicketTypeRequestDto;
import com.ringo.it.util.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventDtoMock {

    public static EventRequestDto getEventDtoMock() {
        return EventRequestDto.builder()
                .id(IdGenerator.getNewId())
                .name("Test")
                .isTicketNeeded(true)
                .description("Test description")
                .categoryIds(new ArrayList<>())
                .ticketTypes(List.of(
                        TicketTypeRequestDto.builder()
                                .title("Test")
                                .description("Test description")
                                .ordinal(0)
                                .price(250.0f)
                                .currencyId(CurrencyDtoMock.getCurrencyDtoMock().getId())
                                .maxTickets(15)
                                .build(),
                        TicketTypeRequestDto.builder()
                                .title("Test 2")
                                .description("Test description 2")
                                .price(35.0f)
                                .ordinal(1)
                                .currencyId(CurrencyDtoMock.getCurrencyDtoMock().getId())
                                .salesStopTime("2029-02-01T01:01:00")
                                .build()
                ))
                .coordinates(new Coordinates(50.0, 50.0))
                .address("Test address")
                .startTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)))
                .endTime(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)))
                .build();
    }
}
