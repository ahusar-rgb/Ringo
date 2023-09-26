package com.ringo.mock.dto;

import com.ringo.dto.company.request.ParticipantRequestDto;
import com.ringo.it.util.IdGenerator;

public class ParticipantDtoMock {
    public static ParticipantRequestDto getParticipantMockDto() {
        return ParticipantRequestDto.builder()
                .name("Test")
                .username("test" + IdGenerator.getNewId())
                .email("test" + IdGenerator.getNewId() + "@ringo-events.com")
                .password("Test$123")
                .dateOfBirth("2000-01-01")
                .gender("MALE")
                .build();
    }
}
