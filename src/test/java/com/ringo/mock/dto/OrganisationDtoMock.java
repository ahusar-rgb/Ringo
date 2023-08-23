package com.ringo.mock.dto;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.it.util.IdGenerator;

import java.util.ArrayList;

public class OrganisationDtoMock {
    public static OrganisationRequestDto getOrganisationDtoMock() {
        return OrganisationRequestDto.builder()
                .name("Test")
                .username("test" + IdGenerator.getNewId())
                .email("test" + IdGenerator.getNewId() + "@test.com")
                .password("test")
                .description("Test description")
                .contacts(new ArrayList<>())
                .build();
    }
}
