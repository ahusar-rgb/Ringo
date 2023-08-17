package com.ringo.mock.model;

import com.ringo.model.company.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class EventMock {
    public static Event getEventMock() {
        return Event.builder()
                .id(System.currentTimeMillis())
                .isActive(true)
                .name("Test")
                .description("Test description")
                .categories(new ArrayList<>())
                .peopleCount(0)
                .price(1.0f)
                .latitude(50.0)
                .longitude(50.0)
                .address("Test address")
                .startTime(LocalDateTime.of(2021, 1, 1, 1, 1))
                .endTime(LocalDateTime.now())
                .host(OrganisationMock.getOrganisationMock())
                .capacity(100)
                .build();
    }
}