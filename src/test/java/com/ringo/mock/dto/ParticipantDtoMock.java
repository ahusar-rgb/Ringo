package com.ringo.mock.dto;

import com.ringo.dto.company.ParticipantRequestDto;

public class ParticipantDtoMock {
    public static ParticipantRequestDto getParticipantMockDto() {
        return ParticipantRequestDto.builder()
                .name("Test")
                .username("test" + System.currentTimeMillis())
                .email("test" + System.currentTimeMillis() + "@test.com")
                .password("test")
                .dateOfBirth("2000-01-01")
                .gender("MALE")
                .build();
    }
}
