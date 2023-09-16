package com.ringo.mock.model;

import com.ringo.it.util.IdGenerator;
import com.ringo.model.company.Event;
import com.ringo.model.company.TicketType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

public class EventMock {
    public static Event getEventMock() {
        return Event.builder()
                .id(IdGenerator.getNewId())
                .isActive(true)
                .name("Test")
                .description("Test description")
                .isTicketNeeded(true)
                .categories(new HashSet<>())
                .peopleCount(0)
                .peopleSaved(0)
                .latitude(50.0)
                .longitude(50.0)
                .ticketTypes(List.of(
                        TicketType.builder()
                                .id(IdGenerator.getNewId())
                                .title("Test")
                                .description("Test description")
                                .price(100.0f)
                                .currency(CurrencyMock.getCurrencyMock())
                                .peopleCount(0)
                                .maxTickets(100)
                                .salesStopTime(LocalDateTime.of(2024, 1, 1, 1, 1))
                                .build(),
                        TicketType.builder()
                                .id(IdGenerator.getNewId())
                                .title("Test 2")
                                .description("Test description 2")
                                .price(200.0f)
                                .currency(CurrencyMock.getCurrencyMock())
                                .peopleCount(0)
                                .maxTickets(100)
                                .salesStopTime(LocalDateTime.of(2024, 1, 1, 1, 1))
                                .build(),
                        TicketType.builder()
                                .id(IdGenerator.getNewId())
                                .title("Test 2")
                                .description("Test description 2")
                                .price(300.0f)
                                .currency(CurrencyMock.getCurrencyMock())
                                .peopleCount(0)
                                .maxTickets(100)
                                .salesStopTime(LocalDateTime.of(2024, 1, 1, 1, 1))
                                .build()
                ))
                .address("Test address")
                .startTime(LocalDateTime.of(2021, 1, 1, 1, 1))
                .endTime(LocalDateTime.of(2025, 1, 1, 1, 1))
                .host(OrganisationMock.getOrganisationMock())
                .build();
    }
}
