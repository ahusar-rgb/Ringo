package com.ringo.mock.dto;

import com.ringo.dto.company.OrganisationRequestDto;

public class OrganisationDtoMock {
    public static OrganisationRequestDto getOrganisationMockDto() {
        return OrganisationRequestDto.builder()
                .name("Test")
                .username("test")
                .email("test@test.com")
                .password("test")
                .description("Test description")
                .contacts("Test contacts")
                .build();
    }
}