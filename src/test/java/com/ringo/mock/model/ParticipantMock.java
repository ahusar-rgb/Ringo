package com.ringo.mock.model;

import com.ringo.model.company.Participant;
import com.ringo.model.enums.Gender;
import com.ringo.model.security.Role;

import java.time.LocalDate;

public class ParticipantMock {
    public static Participant getParticipantMock() {
        return Participant.builder()
                .id(System.currentTimeMillis())
                .isActive(true)
                .email("test@test.com")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .name("Test")
                .username("test")
                .role(Role.ROLE_PARTICIPANT)
                .gender(Gender.MALE)
                .password("test")
                .build();
    }
}
